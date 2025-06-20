package de.throsenheim.inf.sqs.christophpircher.mylibbackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.SearchResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * Data Transfer Object (DTO) for returning book search results to API callers.
 * <p>
 * Represents a paginated list of books returned from the OpenLibrary search endpoint,
 * along with total result count and start index for pagination purposes.
 * </p>
 *
 * <p>This class is typically returned by the {@code /search/external/keyword} endpoint.</p>
 *
 * @see BookDTO
 * @see SearchResult
 * @see de.throsenheim.inf.sqs.christophpircher.mylibbackend.controller.SearchController
 * @see de.throsenheim.inf.sqs.christophpircher.mylibbackend.service.SearchService
 *
 * Author:
 */
@Data
@Builder
public class SearchResultDTO {
    /**
     * Total number of search results available (ignores pagination).
     */
    @JsonProperty("numResults")
    @Schema(description = "Number of total results (without pagination)", example = "801")
    private int numResults;

    /**
     * Index of the first result in the current page (zero-based).
     */
    @JsonProperty("startIndex")
    @Schema(description = "Start index within the total number of search results", example = "100")
    private int startIndex;

    /**
     * List of books returned for the current page.
     */
    @JsonProperty("searchResults")
    //@Schema(description = "Book DTOs of search results")
    private List<BookDTO> searchResults;

    /**
     * Converts a {@link SearchResult} domain model into a {@link SearchResultDTO} for API response.
     *
     * @param searchResult the domain model to convert
     * @return a {@link SearchResultDTO} populated with converted data
     */
    public static SearchResultDTO fromSearchResult(SearchResult searchResult) {
        SearchResultDTOBuilder builder = SearchResultDTO.builder();
        builder.numResults(searchResult.getNumResults());
        builder.startIndex(searchResult.getStartIndex());
        List<BookDTO> bookDTOS = searchResult.getSearchResults().stream().map(BookDTO::fromBook).toList();
        builder.searchResults(bookDTOS);
        return builder.build();
    }
}
