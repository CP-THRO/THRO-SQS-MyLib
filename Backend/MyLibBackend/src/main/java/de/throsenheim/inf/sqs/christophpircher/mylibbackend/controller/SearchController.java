package de.throsenheim.inf.sqs.christophpircher.mylibbackend.controller;

import de.throsenheim.inf.sqs.christophpircher.mylibbackend.dto.ApiError;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.dto.BookDTO;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.dto.SearchResultDTO;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.exceptions.UnexpectedStatusException;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.Book;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.SearchResult;
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

@RestController
@Slf4j
@AllArgsConstructor
@Tag(name="Search")
@RequestMapping("/api/v1/search")
public class SearchController {

    private final SearchService searchService;

    /**
     * Returns the search results from the OpenLibrary API. Calls the searchExternalKeyword of the search service, which will then call the API. The result is converted to the SearchResultDTO.
     * @param keywords Keywords to search for
     * @param startIndex Starting index from which to get the results (for pagination)
     * @param numResultsToGet Number of results to get (for pagination)
     * @throws UnexpectedStatusException The OpenLibrary API has returned an unexpected status code
     * @throws IOException Something went wrong with the connection to the OpenLibrary API
     * @return Response with the search results.
     */
    @Operation(summary = "Keyword search on the OpenLibrary API", description = "Do a keywords search on the OpenLibrary API",
    responses = {
            @ApiResponse(responseCode = "200", description = "Search results", content =  @Content(schema = @Schema(implementation = SearchResultDTO.class))),
            @ApiResponse(responseCode = "502", description = "Something went wrong while accessing the OpenLibrary API (e.g. the server is not responding etc.)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping(value = "/external/keyword", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SearchResultDTO> searchExternalKeyword(@RequestParam(value = "keywords", required = true) String keywords, @RequestParam(value = "startIndex", defaultValue = "0") int startIndex, @RequestParam(value="numResultsToGet", defaultValue = "100")  int numResultsToGet) throws UnexpectedStatusException, IOException {
        log.info("Incoming request to search OpenLibrary with keywords \"{}\"", keywords);
        SearchResult searchResult = searchService.searchKeywordsExternal(keywords,startIndex,numResultsToGet);
        return new ResponseEntity<>(SearchResultDTO.fromSearchResult(searchResult), HttpStatus.OK);
    }

    /**
     * Endpoint for getting a book from the OpenLibrary API by an ISBN
     * @param isbn ISBN to search for
     * @return Response with the book info. Or 404
     * @throws UnexpectedStatusException The OpenLibrary API has returned an unexpected status code
     * @throws IOException Something went wrong with the connection to the OpenLibrary API
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
