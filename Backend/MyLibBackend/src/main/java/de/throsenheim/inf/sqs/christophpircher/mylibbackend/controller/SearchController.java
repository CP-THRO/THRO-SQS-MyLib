package de.throsenheim.inf.sqs.christophpircher.mylibbackend.controller;

import de.throsenheim.inf.sqs.christophpircher.mylibbackend.dto.ApiError;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.dto.BookDTO;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.dto.BookListDTO;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.exceptions.UnexpectedStatusException;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.Book;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.BookList;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.service.SearchService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Optional;

/**
 * REST controller responsible for exposing search functionality to the OpenLibrary API.
 * <p>
 * Provides endpoints for searching books by keyword or ISBN and returns results in DTO format.
 * This controller delegates API interaction logic to the {@link SearchService}.
 * </p>
 *
 * <p>All endpoints are prefixed with <code>/api/v1/search</code>.</p>
 *
 * @see SearchService
 * @see BookListDTO
 * @see BookDTO
 */
@RestController
@Slf4j
@AllArgsConstructor
@Tag(name="Search")
@RequestMapping("/api/v1/search")
public class SearchController {

    private final SearchService searchService;

    /**
     * Searches the OpenLibrary API for books using provided keywords.
     * <p>
     * Supports pagination through {@code startIndex} and {@code numResultsToGet} parameters.
     * Converts the {@link BookList} model into a {@link BookListDTO} for response.
     * </p>
     *
     * @param keywords         The keywords to search for (required)
     * @param startIndex       The result offset for pagination (default = 0)
     * @param numResultsToGet  The number of results to return (default = 100)
     * @return A {@link ResponseEntity} containing the search results or an error
     * @throws UnexpectedStatusException If OpenLibrary returns an unexpected status code
     * @throws IOException If there is a network or API error
     */
    @Operation(summary = "Keyword search on the OpenLibrary API", description = "Do a keywords search on the OpenLibrary API",
    responses = {
            @ApiResponse(responseCode = "200", description = "Search results", content =  @Content(schema = @Schema(implementation = BookListDTO.class))),
            @ApiResponse(responseCode = "502", description = "Something went wrong while accessing the OpenLibrary API (e.g. the server is not responding etc.)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping(value = "/external/keyword", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BookListDTO> searchExternalKeyword(@RequestParam(value = "keywords", required = true) String keywords, @RequestParam(value = "startIndex", defaultValue = "0") int startIndex, @RequestParam(value="numResultsToGet", defaultValue = "100")  int numResultsToGet) throws UnexpectedStatusException, IOException {
        log.info("Incoming request to search OpenLibrary with keywords \"{}\"", keywords);
        BookList searchResult = searchService.searchKeywordsExternal(keywords,startIndex,numResultsToGet);
        return new ResponseEntity<>(BookListDTO.fromSearchResult(searchResult), HttpStatus.OK);
    }

    /**
     * Searches the OpenLibrary API for a book using its ISBN.
     * <p>
     * Converts the resulting {@link Book} to a {@link BookDTO}. Returns 404 if no match is found.
     * </p>
     *
     * @param isbn The ISBN number to search for (required)
     * @return A {@link ResponseEntity} containing the book or a 404 Not Found response
     * @throws UnexpectedStatusException If OpenLibrary returns an unexpected status code
     * @throws IOException If there is a network or API error
     */
    @Operation(summary = "ISBN search on the OpenLibrary API", description = "Searches for a book on the OpenLibrary API by its ISBN number",
    responses = {
            @ApiResponse(responseCode = "200", description = "Details of the book", content = @Content(schema = @Schema(implementation = BookDTO.class))),
            @ApiResponse(responseCode = "404", description = "There is no book with that ISBN in the OpenLibrary API"),
            @ApiResponse(responseCode = "502", description = "Something went wrong while accessing the OpenLibrary API (e.g. the server is not responding etc.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping(value = "/external/isbn", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BookDTO> searchExternalISBN(@RequestParam(value = "isbn", required = true) String isbn) throws UnexpectedStatusException, IOException {
        log.info("Incoming request to search OpenLibrary with isbn \"{}\"", isbn);
        Optional<Book> searchResult = searchService.searchISBNExternal(isbn);
        return searchResult.map(book -> new ResponseEntity<>(BookDTO.fromBook(book), HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
