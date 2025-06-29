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
        log.debug("Request received: GET /get/byID/{}", bookID);
        Optional<Book> book = bookService.getBookById(bookID);

        if (book.isEmpty()) {
            log.info("Book '{}' not found", bookID);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        BookDTO bookDTO = BookDTO.fromBook(book.get());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            User user = ((UserPrincipal) auth.getPrincipal()).getUser();
            log.debug("Authenticated user '{}' - enriching bookDTO with user-specific info", user.getUsername());
            bookDTO.setIndividualRating(bookService.getIndividualRating(bookID, user));
            bookDTO.setReadingStatus(bookService.getReadingStatus(bookID, user));
            bookDTO.setBookIsInLibrary(bookService.isBookInLibrary(bookID, user));
            bookDTO.setBookIsOnWishlist(bookService.isBookOnWishlist(bookID, user));
        }

        return ResponseEntity.ok(bookDTO);
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
    public ResponseEntity<BookListDTO> getAllBooks(@RequestParam(defaultValue = "0") int startIndex,@RequestParam(defaultValue = "100") int numResultsToGet) {
        log.debug("Request received: GET /get/all?startIndex={}&numResultsToGet={}", startIndex, numResultsToGet);
        BookList bookList = bookService.getAllKnownBooks(startIndex, numResultsToGet);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(Util.convertBookListToDTOWithUserSpecificInfoIfAuthenticated(bookList, bookService, auth));
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
    public ResponseEntity<BookListDTO> getAllBooksInLibrary(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestParam(defaultValue = "0") int startIndex, @RequestParam(defaultValue = "100") int numResultsToGet) {
        log.debug("GET /get/library - User: {}", userPrincipal.getUsername());
        BookList list = bookService.getAllBooksInLibrary(startIndex, numResultsToGet, userPrincipal.getUser());
        return ResponseEntity.ok(Util.convertBookListToDTOWithUserSpecificInfo(list, userPrincipal.getUser(), bookService));
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
    public ResponseEntity<BookListDTO> getAllBooksOnWishlist(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestParam(defaultValue = "0") int startIndex, @RequestParam(defaultValue = "100") int numResultsToGet) {
        log.debug("GET /get/wishlist - User: {}", userPrincipal.getUsername());
        BookList list = bookService.getAllBooksOnWishlist(startIndex, numResultsToGet, userPrincipal.getUser());
        return ResponseEntity.ok(Util.convertBookListToDTOWithUserSpecificInfo(list, userPrincipal.getUser(), bookService));
    }

    /**
     * Adds a book to the authenticated user's library.
     *
     * @param userPrincipal the authenticated user's principal
     * @param dto contains the book ID to add
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
    public ResponseEntity<Void> addBookToLibrary(@AuthenticationPrincipal UserPrincipal userPrincipal,  @RequestBody AddBookRequestDTO dto) throws UnexpectedStatusException, BookNotFoundException, IOException {
        log.info("POST /add/library - User: {} adding book: {}", userPrincipal.getUsername(), dto.getBookID());
        bookService.addBookToLibrary(dto.getBookID(), userPrincipal.getUser());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Adds a book to the authenticated user's wishlist.
     *
     * @param userPrincipal the authenticated user's principal
     * @param dto contains the book ID to add
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
    public ResponseEntity<Void> addBookToWishlist(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody AddBookRequestDTO dto) throws UnexpectedStatusException, BookNotFoundException, IOException {
        log.info("POST /add/wishlist - User: {} adding book: {}", userPrincipal.getUsername(), dto.getBookID());
        bookService.addBookToWishList(dto.getBookID(), userPrincipal.getUser());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Updates the user's rating for a specific book in their library.
     *
     * @param userPrincipal the authenticated user's principal
     * @param dto contains the book ID and the new rating
     * @return {@code 200 OK} if the rating was updated
     * @throws BookNotInLibraryException if the book is not in the user's library
     * @throws BookNotFoundException if the book does not exist
     */
    @Operation(summary = "Update the user rating of a book", responses = {
            @ApiResponse(responseCode = "200", description = "Book has been updated with the user rating"),
            @ApiResponse(responseCode = "400", description = "Malformed request: Rating value incorrect"),
            @ApiResponse(responseCode = "404", description = "Book could not be found in the database or in the Library of the user", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "User is not authenticated"),
    })
    @PutMapping(value = "/update/rating")
    public ResponseEntity<Void> updateBookRating(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody ChangeBookRatingDTO dto) throws BookNotFoundException, BookNotInLibraryException {
        log.debug("PUT /update/rating - User: {} updating book: {} with rating: {}", userPrincipal.getUsername(), dto.getBookID(), dto.getRating());
        bookService.rateBook(dto.getBookID(), userPrincipal.getUser(), dto.getRating());
        return ResponseEntity.ok().build();
    }

    /**
     * Updates the user's reading status for a specific book in their library.
     *
     * @param userPrincipal the authenticated user's principal
     * @param dto contains the book ID and the new reading status
     * @return {@code 200 OK} if the status was updated
     * @throws BookNotInLibraryException if the book is not in the user's library
     * @throws BookNotFoundException if the book does not exist
     */
    @Operation(summary = "Update the reading status of a book in the user library", responses = {
            @ApiResponse(responseCode = "200", description = "Book has been updated with the new reading status"),
            @ApiResponse(responseCode = "400", description = "Malformed request: Status value incorrect"),
            @ApiResponse(responseCode = "404", description = "Book could not be found in the database or in the Library of the user", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "User is not authenticated"),
    })
    @PutMapping(value = "/update/status")
    public ResponseEntity<Void> updateReadingStatus(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody ChangeBookReadingStatusRequestDTO dto) throws BookNotFoundException, BookNotInLibraryException {
        log.debug("PUT /update/status - User: {} updating book: {} with status: {}", userPrincipal.getUsername(), dto.getBookID(), dto.getStatus());
        bookService.updateReadingStatus(dto.getBookID(), userPrincipal.getUser(), dto.getStatus());
        return ResponseEntity.ok().build();
    }

    /**
     * Removes a book from the authenticated user's wishlist.
     *
     * @param userPrincipal the authenticated user's principal
     * @param bookID contains the book ID to remove
     * @return {@code 200 OK} if the book was removed
     */
    @Operation(summary = "Remove a book from the wishlist of the user", responses = {
            @ApiResponse(responseCode = "200", description = "Book has been removed"),
            @ApiResponse(responseCode = "403", description = "User is not authenticated")
    })
    @DeleteMapping("/delete/wishlist/{bookID}")
    public ResponseEntity<Void> deleteBookFromWishlist(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                       @PathVariable String bookID) {
        log.info("DELETE /delete/wishlist/{} - User: {}", bookID, userPrincipal.getUsername());
        bookService.removeBookFromWishlist(bookID, userPrincipal.getUser());
        return ResponseEntity.ok().build();
    }

    /**
     * Removes a book from the authenticated user's library
     *
     * @param userPrincipal the authenticated user's principal
     * @param bookID contains the book ID to remove
     * @return {@code 200 OK} if the book was removed
     */
    @Operation(summary = "Remove a book from the library of the user", responses = {
            @ApiResponse(responseCode = "200", description = "Book has been removed"),
            @ApiResponse(responseCode = "403", description = "User is not authenticated")
    })
    @DeleteMapping("/delete/library/{bookID}")
    public ResponseEntity<Void> deleteBookFromLibrary(@AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable String bookID) {
        log.info("DELETE /delete/library/{} - User: {}", bookID, userPrincipal.getUsername());
        bookService.removeBookFromLibrary(bookID, userPrincipal.getUser());
        return ResponseEntity.ok().build();
    }


}
