package de.throsenheim.inf.sqs.christophpircher.mylibbackend.controller;

import de.throsenheim.inf.sqs.christophpircher.mylibbackend.dto.*;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.exceptions.BookNotFoundException;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.exceptions.BookNotInLibraryException;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.exceptions.UnexpectedStatusException;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.Book;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.BookList;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.User;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.service.BookService;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.service.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controller class that handles all HTTP endpoints related to books in the application.
 * This includes operations like retrieving book details, managing the user's personal library
 * and wishlist, and updating metadata like ratings and reading status.
 * All endpoints are accessible under the base path {@code /api/v1/books/}.
 * Authentication is required for user-specific operations like accessing the library or wishlist.
 *
 */
@Controller
@AllArgsConstructor
@Tag(name="Books and Library")
@Slf4j
@RequestMapping("/api/v1/books/")
public class BookController {
    private BookService bookService;

    /**
     * Retrieves book details by its OpenLibrary ID or internal key.
     * Adds user-specific data (rating, status, library/wishlist membership) if the user is authenticated.
     *
     * @param bookID the OpenLibrary book ID
     * @return {@code 200 OK} with {@link BookDTO} if found, {@code 404 Not Found} otherwise
     * @throws UnexpectedStatusException if the OpenLibrary API returned an unexpected HTTP status
     * @throws IOException if there was a communication error with the OpenLibrary API
     */
    @Operation(summary = "Get a book by its OpenLibrary API book id/key", responses = {
            @ApiResponse(responseCode = "200", description = "Details of the book", content = @Content(schema = @Schema(implementation = BookDTO.class))),
            @ApiResponse(responseCode = "404", description = "There is no book with that ID in the database or in the OpenLibrary API"),
            @ApiResponse(responseCode = "502", description = "Something went wrong while accessing the OpenLibrary API (e.g. the server is not responding etc.", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping(value = "/get/byID/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BookDTO> getBookById(@PathVariable("id") String bookID) throws UnexpectedStatusException, IOException {
        Optional<Book> book = bookService.getBookById(bookID);
        if(book.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        BookDTO bookDTO = BookDTO.fromBook(book.get());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        /* Only add rating and reading status if the user is authenticated */
        if (authentication != null &&
                authentication.isAuthenticated() &&
                !(authentication instanceof AnonymousAuthenticationToken)) {
            Object principal = authentication.getPrincipal();
            User user = ((UserPrincipal) principal).getUser();
            bookDTO.setIndividualRating(bookService.getIndividualRating(bookID, user));
            bookDTO.setReadingStatus(bookService.getReadingStatus(bookID, user));
            bookDTO.setBookIsInLibrary(bookService.isBookInLibrary(bookID,  user));
            bookDTO.setBookIsOnWishlist(bookService.isBookOnWishlist(bookID, user));
        }

        return new ResponseEntity<>(bookDTO, HttpStatus.OK);
    }

    /**
     * Retrieves a paginated list of all books in the database.
     *
     * @param startIndex zero-based start index for pagination
     * @param numResultsToGet number of books to retrieve starting from {@code startIndex}
     * @return {@code 200 OK} with {@link BookListDTO} object
     */
    @Operation(summary = "Get a list of all books. Paginated.", responses = {
            @ApiResponse(responseCode = "200", description = "List with all books in the database (paginated)", content = @Content(schema = @Schema(implementation = BookDTO.class))),
    })
    @GetMapping(value = "/get/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BookListDTO> getAllBooks(@RequestParam(value = "startIndex", defaultValue = "0") int startIndex, @RequestParam(value="numResultsToGet", defaultValue = "100") int numResultsToGet){
        BookList bookList = bookService.getAllKnownBooks(startIndex, numResultsToGet);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        /* Only add rating and reading status if the user is authenticated */
        if (authentication != null &&
                authentication.isAuthenticated() &&
                !(authentication instanceof AnonymousAuthenticationToken)) {
            Object principal = authentication.getPrincipal();
            User user = ((UserPrincipal) principal).getUser();
            return new ResponseEntity<>(convertBookListToDTOWithUserSpecificInfo(bookList,user), HttpStatus.OK);
        }else{
            return new ResponseEntity<>(BookListDTO.fromSearchResult(bookList), HttpStatus.OK);
        }
    }

    /**
     * Retrieves a paginated list of books in the authenticated user's library.
     * Also includes user-specific metadata per book.
     *
     * @param userPrincipal the authenticated user's principal
     * @param startIndex zero-based start index for pagination
     * @param numResultsToGet number of books to retrieve starting from {@code startIndex}
     * @return {@code 200 OK} with list of {@link BookDTO}s
     */
    @Operation(summary = "Get a list of all books in the library of a user. Paginated.", responses = {
            @ApiResponse(responseCode = "200", description = "List with all books in the library (paginated)", content = @Content(schema = @Schema(implementation = BookDTO.class))),
            @ApiResponse(responseCode = "403", description = "User is not authenticated")
    })
    @GetMapping(value ="/get/library", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BookListDTO> getAllBooksInLibrary(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestParam(value = "startIndex", defaultValue = "0") int startIndex, @RequestParam(value="numResultsToGet", defaultValue = "100") int numResultsToGet) {
        BookList bookList = bookService.getAllBooksInLibrary(startIndex, numResultsToGet, userPrincipal.getUser());
        return new ResponseEntity<>(convertBookListToDTOWithUserSpecificInfo(bookList, userPrincipal.getUser()), HttpStatus.OK);
    }

    /**
     * Retrieves a paginated list of books in the authenticated user's wishlist.
     *
     * @param userPrincipal the authenticated user's principal
     * @param startIndex zero-based start index for pagination
     * @param numResultsToGet number of books to retrieve starting from {@code startIndex}
     * @return {@code 200 OK} with list of {@link BookDTO}s
     */
    @Operation(summary = "Get a list of all books on the wishlist of a user. Paginated.", responses = {
            @ApiResponse(responseCode = "200", description = "List with all books in the database (paginated)", content = @Content(schema = @Schema(implementation = BookDTO.class))),
            @ApiResponse(responseCode = "403", description = "User is not authenticated")
    })
    @GetMapping(value ="/get/wishlist", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BookListDTO> getAllBooksOnWishlist(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestParam(value = "startIndex", defaultValue = "0") int startIndex, @RequestParam(value="numResultsToGet", defaultValue = "100") int numResultsToGet) {
        BookList bookList = bookService.getAllBooksOnWishlist(startIndex, numResultsToGet, userPrincipal.getUser());
        return new ResponseEntity<>(BookListDTO.fromSearchResult(bookList), HttpStatus.OK);
    }

    /**
     * Adds a book to the authenticated user's library.
     *
     * @param userPrincipal the authenticated user's principal
     * @param addBookRequestDTO contains the book ID to add
     * @return {@code 201 Created} if successful
     * @throws UnexpectedStatusException if the OpenLibrary API returned an unexpected HTTP status
     * @throws BookNotFoundException if the book was not found
     * @throws IOException if an I/O error occurred while fetching book data
     */
    @Operation(summary = "Add a book to the library of the user", responses = {
            @ApiResponse(responseCode = "201", description = "Book has been added to the library"),
            @ApiResponse(responseCode = "404", description = "Book could not be found in the database or in the OpenLibrary", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "User is not authenticated"),
            @ApiResponse(responseCode = "502", description = "Something went wrong while accessing the OpenLibrary API (e.g. the server is not responding etc.", content =  @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping(value = "/add/library")
    public ResponseEntity<Void> addBookToLibrary(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody AddBookRequestDTO addBookRequestDTO) throws UnexpectedStatusException, BookNotFoundException, IOException {
        bookService.addBookToLibrary(addBookRequestDTO.getBookID(), userPrincipal.getUser());
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * Adds a book to the authenticated user's wishlist.
     *
     * @param userPrincipal the authenticated user's principal
     * @param addBookRequestDTO contains the book ID to add
     * @return {@code 201 Created} if successful
     * @throws UnexpectedStatusException if the OpenLibrary API returned an unexpected HTTP status
     * @throws BookNotFoundException if the book was not found
     * @throws IOException if an I/O error occurred while fetching book data
     */
    @Operation(summary = "Add a book to the wishlist of the user", responses = {
            @ApiResponse(responseCode = "201", description = "Book has been added to the wishlist"),
            @ApiResponse(responseCode = "404", description = "Book could not be found in the database or in the OpenLibrary", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "User is not authenticated"),
            @ApiResponse(responseCode = "502", description = "Something went wrong while accessing the OpenLibrary API (e.g. the server is not responding etc.", content =  @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping(value = "/add/wishlist")
    public ResponseEntity<Void> addBookToWishlist(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody AddBookRequestDTO addBookRequestDTO) throws UnexpectedStatusException, BookNotFoundException, IOException {
        bookService.addBookToWishList(addBookRequestDTO.getBookID(), userPrincipal.getUser());
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * Updates the user's rating for a specific book in their library.
     *
     * @param userPrincipal the authenticated user's principal
     * @param changeBookRatingDTO contains the book ID and the new rating
     * @return {@code 200 OK} if the rating was updated
     * @throws BookNotInLibraryException if the book is not in the user's library
     * @throws BookNotFoundException if the book does not exist
     */
    @Operation(summary = "Update the user rating of a book", responses = {
            @ApiResponse(responseCode = "200", description = "Book has been updated with the user rating"),
            @ApiResponse(responseCode = "404", description = "Book could not be found in the database or in the Library of the user", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "User is not authenticated"),
    })
    @PutMapping(value = "/update/rating")
    public ResponseEntity<Void> updateBookRating(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody ChangeBookRatingDTO changeBookRatingDTO) throws BookNotInLibraryException, BookNotFoundException {
        bookService.rateBook(changeBookRatingDTO.getBookID(), userPrincipal.getUser(),changeBookRatingDTO.getRating());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Updates the user's reading status for a specific book in their library.
     *
     * @param userPrincipal the authenticated user's principal
     * @param changeBookReadingStatusRequestDTO contains the book ID and the new reading status
     * @return {@code 200 OK} if the status was updated
     * @throws BookNotInLibraryException if the book is not in the user's library
     * @throws BookNotFoundException if the book does not exist
     */
    @Operation(summary = "Update the reading status of a book in the user library", responses = {
            @ApiResponse(responseCode = "200", description = "Book has been updated with the new reading status"),
            @ApiResponse(responseCode = "404", description = "Book could not be found in the database or in the Library of the user", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "User is not authenticated"),
    })
    @PutMapping(value = "/update/status")
    public ResponseEntity<Void> updateReadingStatus(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody ChangeBookReadingStatusRequestDTO changeBookReadingStatusRequestDTO) throws BookNotInLibraryException, BookNotFoundException {
        bookService.updateReadingStatus(changeBookReadingStatusRequestDTO.getBookID(), userPrincipal.getUser(), changeBookReadingStatusRequestDTO.getStatus());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Removes a book from the authenticated user's wishlist.
     *
     * @param userPrincipal the authenticated user's principal
     * @param addBookRequestDTO contains the book ID to remove
     * @return {@code 200 OK} if the book was removed
     */
    @Operation(summary = "Remove a book from the wishlist of the user", responses = {
            @ApiResponse(responseCode = "200", description = "Book has been removed"),
            @ApiResponse(responseCode = "403", description = "User is not authenticated")
    })
    @DeleteMapping("/delete/wishlist")
    public ResponseEntity<Void> deleteBookFromWishlist(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody AddBookRequestDTO addBookRequestDTO){
        bookService.removeBookFromWishlist(addBookRequestDTO.getBookID(), userPrincipal.getUser());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Converts a {@link BookList} into a {@link BookListDTO}, enriching each {@link BookDTO}
     * with user-specific metadata such as individual rating, reading status,
     * library membership, and wishlist membership.
     * <p>
     * This method is intended to be used for authenticated users, where personalization
     * of book data is needed.
     * </p>
     *
     * @param bookList the list of books to convert, including pagination metadata
     * @param user     the authenticated user whose library and wishlist data will be used
     * @return a fully populated {@link BookListDTO} containing enriched {@link BookDTO} entries
     *
     * @see BookService#getIndividualRating(String, User)
     * @see BookService#getReadingStatus(String, User)
     * @see BookService#isBookInLibrary(String, User)
     * @see BookService#isBookOnWishlist(String, User)
     */
    private BookListDTO convertBookListToDTOWithUserSpecificInfo(BookList bookList, User user){
        BookListDTO.BookListDTOBuilder bookListDTOBuilder = BookListDTO.builder();
        bookListDTOBuilder.startIndex(bookList.getStartIndex());
        bookListDTOBuilder.numResults(bookList.getNumResults());
        bookListDTOBuilder.skippedBooks(bookList.getSkippedBooks());
        List<BookDTO> bookDTOs = new ArrayList<>(bookList.getBooks().size());
        for (Book book : bookList.getBooks()) { //need cannot really use fromBook() method here, since I need to set some additional information
            BookDTO bookDTO = BookDTO.fromBook(book);
            bookDTO.setIndividualRating(bookService.getIndividualRating(book.getBookID(), user));
            bookDTO.setReadingStatus(bookService.getReadingStatus(book.getBookID(), user));
            bookDTO.setBookIsInLibrary(bookService.isBookInLibrary(book.getBookID(), user));
            bookDTO.setBookIsOnWishlist(bookService.isBookOnWishlist(book.getBookID(), user));
            bookDTOs.add(bookDTO);
        }
        bookListDTOBuilder.books(bookDTOs);
        return bookListDTOBuilder.build();
    }
}
