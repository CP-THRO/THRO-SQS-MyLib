package de.throsenheim.inf.sqs.christophpircher.mylibbackend.api;

import de.throsenheim.inf.sqs.christophpircher.mylibbackend.api.dto.*;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.exceptions.UnexpectedStatusException;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.Book;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
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
import java.util.concurrent.CompletableFuture;

/**
 * Proxy implementation of the OpenLibrary API.
 */
@Service
@Slf4j
public class OpenLibraryAPI {
    private interface OpenLibraryAPIInterface {
        @GET("/search.json")
        Call<OpenLibraryAPISearchResponse>  search(@Query("q") String keywords, @Query("offset") int startingIndex, @Query("limit") int limit);

        @GET("/books/{bookID}.json")
        Call<OpenLibraryAPIBook> getBookById(@Path("bookID") String bookId);

        @GET("/isbn/{isbn}.json")
        Call<OpenLibraryAPIBook> getBookByIsbn(@Path("isbn") String isbn);

        @GET("/works/{workID}.json")
        Call<OpenLibraryAPIWork> getWorkById(@Path("workID") String workId);

        @GET("/authors/{authorID}.json")
        Call<OpenLibraryAPIAuthor> getAuthorById(@Path("authorID") String authorId);

        @GET("/works/{workID}/editions.json")
        Call<OpenLibraryAPIEditions> getEditionsByWorkId(@Path("workID") String workId);

    }

    private OpenLibraryAPIInterface api = null;
    private final Environment environment;

    private static final String UNEXPECTED_STATUS_MESSAGE = "OpenLibraryAPI: Unexpected status code: ";

    /**
     * Instantiate the class with the SpringBoot environment. This is called automatically by SpringBoot on startup.
     * @param environment SpringBoot environment
     */
    public OpenLibraryAPI(Environment environment) {
        this.environment = environment;
    }

    /**
     * Searches for a book by keywords.
     * @param searchString String with the keywords to search for
     * @param startingIndex Starting search result index. For pagination.
     * @param numResultsToGet The number of results to get, starting from the startingIndex. For pagination.
     * @return A search result object. No optional, since there is always a valid response.
     */
    public SearchResult searchBooks(String searchString, int startingIndex, int numResultsToGet) throws UnexpectedStatusException, IOException {
        if(api == null) {
            createNewApi();
        }
        log.info("Searching for keywords \"{}\"", searchString);
        searchString = searchString.trim().replaceAll("\\s", "+"); // replace whitespaces with "+", because the API requires it.

        Call<OpenLibraryAPISearchResponse> apiSearchCall = api.search(searchString, startingIndex, numResultsToGet);

        try {
            Response<OpenLibraryAPISearchResponse> apiSearchResponse = apiSearchCall.execute();
            if(apiSearchResponse.isSuccessful()  && apiSearchResponse.body() != null) {
                OpenLibraryAPISearchResponse response = apiSearchResponse.body();
                SearchResult.SearchResultBuilder builder = SearchResult.builder();
                builder.numResults(response.getNumFound());
                builder.startIndex(response.getStart());

                List<OpenLibraryAPISearchWork> searchWorks = response.getSearchResults();
                /*
                 * In theory, all search results contain part of the information. But since I need to call the Books API anyway for the ISBN, and I need that API for storing books in a personal library, I am getting everything from there.
                 */
                List<CompletableFuture<Optional<Book>>> futures = new ArrayList<>();
                for (OpenLibraryAPISearchWork work : searchWorks) {
                    String coverEditionKey = work.getCoverEditionKey();
                    if(coverEditionKey == null) { // basically: If there is a book without a cover, I have to get an edition to get a book ID.
                        OpenLibraryAPIEditions editions = getWorkEditionsByID(work.getWorkKeyWithoutURL());

                        String editionBookKey = null;

                        for(int i = 0; i < editions.getEditions().size(); i++) {
                            editionBookKey = editions.getEditions().get(i).getBookKeyWithoutURL();
                            if(editionBookKey != null) {
                                coverEditionKey = editionBookKey;
                                break;
                            }
                        }
                    }

                    if(coverEditionKey != null) { //Skip book if no book key can be found
                        futures.add(getBookByBookIDAsync(coverEditionKey));
                    }
                }

                // Wait for all to finish
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

                // Collect successful books
                List<Book> books = futures.stream()
                        .map(CompletableFuture::join)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .toList();


                builder.searchResults(books);
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
     * Get the Information of a book by searching for a specific ISBN.
     * @param isbn ISBN to get the book for
     * @return Optional with Book object with all the relevant information. Optional because the ISBN might not exist, since it is supplied by my own api endpoint
     */
    public Optional<Book> getBookByISBN(String isbn) throws UnexpectedStatusException, IOException {
        if(api == null) {
            createNewApi();
        }

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
     * Get A book by its OpenLibrary API key
     * @param bookID Book ID of the book to get
     * @return Book object with all relevant info. Optional, because a wrong bookid may be supplied on my own API endpoint.
     */
    public Optional<Book> getBookByBookID(String bookID) throws IOException, UnexpectedStatusException {
        if(api == null) {
            createNewApi();
        }
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
                    int coverID = bookDTO.getCoverIDs().getFirst();
                    String coverURLTemplate = "https://covers.openlibrary.org/b/id/%d-%s.jpg";
                    bookBuilder.coverURLSmall(String.format(coverURLTemplate, coverID, "S"));
                    bookBuilder.coverURLMedium(String.format(coverURLTemplate, coverID, "M"));
                    bookBuilder.coverURLLarge(String.format(coverURLTemplate, coverID, "L"));
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

    @Async
    public CompletableFuture<Optional<Book>> getBookByBookIDAsync(String bookID) {
        try {
            return CompletableFuture.completedFuture(getBookByBookID(bookID));
        } catch (Exception e) {
            log.error("Failed to fetch book {}", bookID, e);
            return CompletableFuture.completedFuture(Optional.empty());
        }
    }

    /**
     * Helper function to get the work information by work id. Used to get the description of the book/work
     * @param workID Work ID to get the information for.
     * @return Work DTO. No model class, since this is internal for the API proxy. Also no Optional, since a book is always associated with a work.
     */
    private OpenLibraryAPIWork getWorkByWorkID(String workID) throws IOException, UnexpectedStatusException {
        if(api == null) {
            createNewApi();
        }

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
     * Helper function to get the name of an author by autor id.
     * @param authorID Id of the author to get
     * @return Author DTO object. No Optional, since I am taking the ID straight from the Book API results. If that is broken
     */
    private OpenLibraryAPIAuthor getAuthorByAuthorID(String authorID) throws UnexpectedStatusException, IOException {
        if(api == null) {
            createNewApi();
        }
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
     * Helper function to get the editions of a work.
     * @param workID The work ID to get the editions for.
     * @return Object with a list of all editions with their keys. I will take the first one with a key != 0. There must be at least one edition for there to be a work, so no optional-
     * @throws UnexpectedStatusException OpenLibrary API returned unexpected status code
     * @throws IOException Something went wrong with the connection
     */
    private OpenLibraryAPIEditions getWorkEditionsByID(String workID) throws UnexpectedStatusException, IOException {
        if(api == null) {
            createNewApi();
        }
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
     * Lazy-instantiates the api object.
     * Required for WireMock:
     * In order to test with WireMock I need the baseURL to be configured in the application properties,
     * but I cannot access the spring environment in the constructor.
     */
    private void createNewApi() {
        log.info("Creating OpenLibraryAPI object");
        String baseurl = environment.getProperty("external.openLibraryAPIBaseURL");
        assert baseurl != null;
        Retrofit retrofit = new Retrofit.Builder().baseUrl(baseurl).addConverterFactory(JacksonConverterFactory.create()).build();
        api = retrofit.create(OpenLibraryAPIInterface.class);
    }

    /** Alter an IO exception for better logging. */
    private IOException alterIOException(IOException original) {
        IOException altered = new IOException("OpenLibraryAPI: " + original.getMessage());
        altered.setStackTrace(original.getStackTrace());
        return altered;
    }


}
