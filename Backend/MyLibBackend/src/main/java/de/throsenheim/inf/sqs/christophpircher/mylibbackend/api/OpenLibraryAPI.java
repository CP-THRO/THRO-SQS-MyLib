package de.throsenheim.inf.sqs.christophpircher.mylibbackend.api;

import de.throsenheim.inf.sqs.christophpircher.mylibbackend.api.dto.*;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.exceptions.UnexpectedStatusException;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.Book;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.BookList;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service that acts as a proxy for interacting with the OpenLibrary API using Retrofit.
 * <p>
 * It wraps multiple OpenLibrary endpoints, including search, book lookup by ID or ISBN,
 * work and author retrieval, and fallback handling for incomplete data (e.g., missing covers editions).
 * </p>
 *
 * <p>This proxy also performs transformation into the application's internal {@link Book} and {@link BookList} models.</p>
 *
 * <p>The API base URL is dynamically loaded from Spring's {@link Environment} to support environments like WireMock during testing.</p>
 *
 */
@Service
@Slf4j
public class OpenLibraryAPI {

    /**
     * Retrofit interface defining all OpenLibrary REST API endpoints used.
     */
    private interface OpenLibraryAPIInterface {
        /**
         * Search for works (books) by keyword.
         */
        @GET("/search.json")
        Call<OpenLibraryAPISearchResponse>  search(@Query("q") String keywords, @Query("offset") int startingIndex, @Query("limit") int limit);

        /**
         * Retrieve detailed book information by book ID.
         */
        @GET("/books/{bookID}.json")
        Call<OpenLibraryAPIBook> getBookById(@Path("bookID") String bookId);

        /**
         * Retrieve book data by ISBN.
         */
        @GET("/isbn/{isbn}.json")
        Call<OpenLibraryAPIBook> getBookByIsbn(@Path("isbn") String isbn);

        /**
         * Retrieve work data by work ID.
         */
        @GET("/works/{workID}.json")
        Call<OpenLibraryAPIWork> getWorkById(@Path("workID") String workId);

        /**
         * Retrieve author data by author ID.
         */
        @GET("/authors/{authorID}.json")
        Call<OpenLibraryAPIAuthor> getAuthorById(@Path("authorID") String authorId);

        /**
         * Retrieve all editions associated with a work.
         */
        @GET("/works/{workID}/editions.json")
        Call<OpenLibraryAPIEditions> getEditionsByWorkId(@Path("workID") String workId);
    }

    private OpenLibraryAPIInterface api = null;
    private final Environment environment;

    private static final String UNEXPECTED_STATUS_MESSAGE = "OpenLibraryAPI: Unexpected status code: ";

    /**
     * Constructor used by Spring Boot to inject environment variables.
     *
     * @param environment Spring Boot environment object for accessing properties like base URL.
     */
    public OpenLibraryAPI(Environment environment) {
        this.environment = environment;
    }

    /**
     * Searches for books using keywords and returns a structured {@link BookList}.
     * <p>
     * Handles pagination and fallback logic when some results are missing data.
     * </p>
     *
     * @param searchString The keyword(s) to search for.
     * @param startingIndex The result offset for pagination.
     * @param numResultsToGet Number of results to return.
     * @return A {@link BookList} containing found books.
     * @throws UnexpectedStatusException if OpenLibrary returns a non-200 status code.
     * @throws IOException if a connection or parsing error occurs.
     */
    public BookList searchBooks(String searchString, int startingIndex, int numResultsToGet) throws UnexpectedStatusException, IOException {
        log.info("Searching for keywords \"{}\"", searchString);
        searchString = searchString.trim().replaceAll("\\s", "+"); // replace whitespaces with "+", because the API requires it.

        Call<OpenLibraryAPISearchResponse> apiSearchCall = api.search(searchString, startingIndex, numResultsToGet);

        try {
            Response<OpenLibraryAPISearchResponse> apiSearchResponse = apiSearchCall.execute();
            if(apiSearchResponse.isSuccessful()  && apiSearchResponse.body() != null) {
                OpenLibraryAPISearchResponse response = apiSearchResponse.body();
                BookList.BookListBuilder builder = BookList.builder();
                builder.numResults(response.getNumFound());
                builder.startIndex(response.getStart());

                List<OpenLibraryAPISearchWork> searchWorks = response.getSearchResults();
                List<Book> books = new ArrayList<>(searchWorks.size());

                int skippedBooks = 0;

                for (OpenLibraryAPISearchWork work : searchWorks) {
                    String coverEditionKey = work.getCoverEditionKey();
                    if(coverEditionKey == null) { // basically: If there is a book without a cover, I have to get an edition to get a book ID.
                        OpenLibraryAPIEditions editions = getWorkEditionsByID(work.getWorkKeyWithoutURL());
                        if(!editions.getEditions().isEmpty()){ // this can actually happen... The API is so scuffed. Skip
                            coverEditionKey = editions.getEditions().getFirst().getBookKeyWithoutURL(); //an edition must have its own key. I am taking the first, since it does not really matter.
                        }else{
                            ++skippedBooks;
                            continue; // skip it.
                        }
                    }

                    Book.BookBuilder bookBuilder = Book.builder();
                    bookBuilder.bookID(coverEditionKey);
                    bookBuilder.title(work.getTitle());
                    bookBuilder.authors(work.getAuthors());
                    bookBuilder.publishDate(Integer.toString(work.getFirstPublishYear()));

                    String[] coverURLs = getCoverURLs(work.getCoverID());
                    bookBuilder.coverURLSmall(coverURLs[0]);
                    bookBuilder.coverURLMedium(coverURLs[1]);
                    bookBuilder.coverURLLarge(coverURLs[2]);

                    books.add(bookBuilder.build());
                }

                builder.books(books);
                builder.skippedBooks(skippedBooks);
                return builder.build();
            }else{
                log.error("OpenLibraryAPI: Could not search OpenLibraryAPI: {} {}", apiSearchResponse.code(), apiSearchResponse.message());
                throw new UnexpectedStatusException(UNEXPECTED_STATUS_MESSAGE + apiSearchResponse.code());
            }

        }catch (IOException e) {
            throw alterIOException(e);
        }
    }

    /**
     * Retrieves a book using its ISBN.
     *
     * @param isbn ISBN to search for.
     * @return An Optional {@link Book}, or empty if not found (404).
     * @throws UnexpectedStatusException if OpenLibrary returns a non-200 or unexpected status.
     * @throws IOException if a network error occurs.
     */
    public Optional<Book> getBookByISBN(String isbn) throws UnexpectedStatusException, IOException {
        Call<OpenLibraryAPIBook> call = api.getBookByIsbn(isbn);
        try {
            Response<OpenLibraryAPIBook> response = call.execute();
            if(response.isSuccessful() && response.body() != null) {
                OpenLibraryAPIBook book = response.body(); // the body is essentialy the same as from the /books/bookid endpoint. But to not duplicate the code, I let handle getBookByBookID all the work
                return getBookByBookID(book.getBookIDWithoutURL());
            } else if (response.code() == 404) {
                return Optional.empty();
            }else{
                log.error("OpenLibraryAPI: Could not get book with ISBN {}: {} {}", isbn, response.code(), response.message());
                throw new UnexpectedStatusException(UNEXPECTED_STATUS_MESSAGE + response.code());
            }
        }catch (IOException e) {
            throw alterIOException(e);
        }
    }

    /**
     * Retrieves a book using its OpenLibrary book ID.
     *
     * @param bookID Book ID (e.g., "OL12345M").
     * @return Optional {@link Book}, empty if not found (404).
     * @throws IOException if the request fails.
     * @throws UnexpectedStatusException if the API response is invalid or unexpected.
     */
    public Optional<Book> getBookByBookID(String bookID) throws IOException, UnexpectedStatusException {
        Call<OpenLibraryAPIBook> call = api.getBookById(bookID);
        try{
            Response<OpenLibraryAPIBook> bookResponse = call.execute();
            if(bookResponse.isSuccessful() && bookResponse.body() != null) {
                OpenLibraryAPIBook bookDTO = bookResponse.body();
                Book.BookBuilder bookBuilder = Book.builder();
                bookBuilder.title(bookDTO.getTitle());
                bookBuilder.subtitle(bookDTO.getSubtitle());
                bookBuilder.publishDate(bookDTO.getPublishDate());
                bookBuilder.bookID(bookDTO.getBookIDWithoutURL());

                if(!bookDTO.getCoverIDs().isEmpty()){
                    String[] coverURLs = getCoverURLs(bookDTO.getCoverIDs().getFirst());
                    bookBuilder.coverURLSmall(coverURLs[0]);
                    bookBuilder.coverURLMedium(coverURLs[1]);
                    bookBuilder.coverURLLarge(coverURLs[2]);
                }

                OpenLibraryAPIWork work = getWorkByWorkID(bookDTO.getWorkKeys().getFirst().getKeyWithoutURL());
                bookBuilder.description(work.getDescription().getValue());

                List<String> authors = new ArrayList<>(work.getAuthors().size());

                List<OpenLibraryAPIWork.Author> dtoAuthorList = work.getAuthors();

                if(!dtoAuthorList.isEmpty()) {
                    for(OpenLibraryAPIWork.Author author : dtoAuthorList){
                        authors.add(getAuthorByAuthorID(author.getAuthorKey().getKeyWithoutURL()).getName());
                    }
                }else{
                    authors.add("No author found"); //the only author info is in the search api... but I cannot do that outside of the search endpoint
                }

                bookBuilder.authors(authors);

                List<String> isbns = new ArrayList<>(bookDTO.getIsbn10s().size() + bookDTO.getIsbn13s().size());
                isbns.addAll(bookDTO.getIsbn10s());
                isbns.addAll(bookDTO.getIsbn13s());
                bookBuilder.isbns(isbns);
                return Optional.of(bookBuilder.build());
            }else{
                if(bookResponse.code() == 404){
                    return Optional.empty();
                }else{
                    log.error("OpenLibraryAPI: Could not get book with ID {}: {} {}", bookID, bookResponse.code(), bookResponse.message());
                    throw new UnexpectedStatusException(UNEXPECTED_STATUS_MESSAGE + bookResponse.code());
                }
            }
        } catch (IOException e) {
            throw alterIOException(e);
        }
    }

    /**
     * Retrieves metadata for a work using its ID.
     *
     * @param workID ID of the work (e.g., "OL123456W").
     * @return {@link OpenLibraryAPIWork} object with description and author info.
     * @throws IOException if a request fails.
     * @throws UnexpectedStatusException if the response status is not successful.
     */
    private OpenLibraryAPIWork getWorkByWorkID(String workID) throws IOException, UnexpectedStatusException {
        Call<OpenLibraryAPIWork> call = api.getWorkById(workID);
        try{
            Response<OpenLibraryAPIWork> workResponse = call.execute();
            if(workResponse.isSuccessful() && workResponse.body() != null) {
                return workResponse.body();
            }else{
                log.error("OpenLibraryAPI: Could not get work with ID {}: {} {}", workID, workResponse.code(), workResponse.message());
                throw new UnexpectedStatusException(UNEXPECTED_STATUS_MESSAGE + workResponse.code());
            }
        } catch (IOException e){
            throw alterIOException(e);
        }
    }


    /**
     * Retrieves metadata for an author by their ID.
     *
     * @param authorID OpenLibrary author ID (e.g., "OL12345A").
     * @return An {@link OpenLibraryAPIAuthor} DTO.
     * @throws UnexpectedStatusException if OpenLibrary returns an error.
     * @throws IOException on network failure.
     */
    private OpenLibraryAPIAuthor getAuthorByAuthorID(String authorID) throws UnexpectedStatusException, IOException {
        Call<OpenLibraryAPIAuthor> call = api.getAuthorById(authorID);
        try {
            Response<OpenLibraryAPIAuthor> response = call.execute();
            if(response.isSuccessful() && response.body() != null) {
                return response.body();
            }else{
                log.error("OpenLibraryAPI: Could not get author with ID {}: {} {}", authorID, response.code(), response.message());
                throw new UnexpectedStatusException(UNEXPECTED_STATUS_MESSAGE + response.code());
            }
        } catch (IOException e) {
            throw alterIOException(e);
        }
    }

    /**
     * Retrieves all editions of a work to fall back when no direct cover edition is available.
     *
     * @param workID ID of the work to fetch editions for.
     * @return An {@link OpenLibraryAPIEditions} DTO with edition entries.
     * @throws UnexpectedStatusException if OpenLibrary returns an error.
     * @throws IOException on connection failure.
     */
    private OpenLibraryAPIEditions getWorkEditionsByID(String workID) throws UnexpectedStatusException, IOException {
        Call<OpenLibraryAPIEditions> call = api.getEditionsByWorkId(workID);
        try {
            Response<OpenLibraryAPIEditions> response = call.execute();
            if(response.isSuccessful() && response.body() != null) {
                return response.body();
            }else{
                log.info("OpenLibraryAPI: Could not get editions for work ID {}: {} {}", workID, response.code(), response.message());
                throw new UnexpectedStatusException(UNEXPECTED_STATUS_MESSAGE + response.code());
            }
        } catch (IOException e) {
            throw alterIOException(e);
        }

    }

    /**
     * Initializes the Retrofit client after dependency injection completes.
     * <p>
     * Loads the base URL from application properties, allowing dynamic switching for test environments.
     * </p>
     */
    @PostConstruct
    private void createNewApi() {
        log.info("Creating OpenLibraryAPI object");
        String baseurl = environment.getProperty("external.openLibraryAPIBaseURL");
        assert baseurl != null;
        Retrofit retrofit = new Retrofit.Builder().baseUrl(baseurl).addConverterFactory(JacksonConverterFactory.create()).build();
        api = retrofit.create(OpenLibraryAPIInterface.class);
    }


    /**
     * Creates a custom IOException with a prefixed message for consistent logging.
     *
     * @param original The original IOException.
     * @return Modified IOException with additional context.
     */
    private IOException alterIOException(IOException original) {
        IOException altered = new IOException("OpenLibraryAPI: " + original.getMessage());
        altered.setStackTrace(original.getStackTrace());
        return altered;
    }


    /**
     * Builds full-size image URLs for a given cover ID.
     *
     * @param coverID The numeric ID of the cover.
     * @return A String array with URLs in small [0], medium [1], and large [2] sizes.
     */
    private String[] getCoverURLs(int coverID){
        String[] coverURLs = new String[3];
        String coverURLTemplate = "https://covers.openlibrary.org/b/id/%d-%s.jpg";
        coverURLs[0] =String.format(coverURLTemplate, coverID, "S");
        coverURLs[1] =String.format(coverURLTemplate, coverID, "M");
        coverURLs[2] =String.format(coverURLTemplate, coverID, "L");

        return coverURLs;
    }


}
