package de.throsenheim.inf.sqs.christophpircher.mylibbackend.controller;

import de.throsenheim.inf.sqs.christophpircher.mylibbackend.dto.ApiError;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.dto.BookDTO;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.exceptions.UnexpectedStatusException;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.Book;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.repository.BookRepository;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.service.BookService;
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
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Controller
@AllArgsConstructor
@Tag(name="book")
@Slf4j
@RequestMapping("/api/v1/books/")
public class BookController {
    private BookService bookService;

    /**
     * Endpoint for getting a book by its OpenLibrary API bookID / key
     * @param bookID OpenLibrary API book id string
     * @return Book information. Or 404
     */
    @Operation(summary = "Get a book by its OpenLibrary API book id/key", responses = {
            @ApiResponse(responseCode = "200", description = "Details of the book", content = @Content(schema = @Schema(implementation = BookDTO.class))),
            @ApiResponse(responseCode = "404", description = "There is no book with that ID in the database or in the OpenLibrary API"),
            @ApiResponse(responseCode = "502", description = "Something went wrong while accessing the OpenLibrary API (e.g. the server is not responding etc.", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping(value = "/get/byID/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BookDTO> getBookById(@PathVariable("id") String bookID) throws UnexpectedStatusException, IOException {
        Optional<Book> book = bookService.getBookById(bookID);


        return book.map(value -> new ResponseEntity<>(BookDTO.fromBook(value), HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Get a (paginated) list of all books stored in the database, i.e. that are part of a library
     * @param startIndex Start index to start getting the results from. For pagination.
     * @param numResultsToGet Number of books to get from the starting index onwards. For pagination
     * @return List of books
     */
    @Operation(summary = "Get a list of all books. Paginated.", responses = {
            @ApiResponse(responseCode = "200", description = "List with all books in the database", content = @Content(schema = @Schema(implementation = BookDTO.class))),
    })
    @GetMapping(value = "/get/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<BookDTO>> getAllBooks(@RequestParam(value = "startIndex", defaultValue = "0") int startIndex, @RequestParam(value="numResultsToGet", defaultValue = "100") int numResultsToGet){
        List<Book> books = bookService.getAllKnownBooks(startIndex, numResultsToGet);
        return new ResponseEntity<>(books.stream().map(BookDTO::fromBook).toList(), HttpStatus.OK);
    }
}
