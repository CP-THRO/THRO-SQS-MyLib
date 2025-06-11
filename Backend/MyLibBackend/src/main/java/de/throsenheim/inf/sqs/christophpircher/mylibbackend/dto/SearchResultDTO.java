package de.throsenheim.inf.sqs.christophpircher.mylibbackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.SearchResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * Class to transfer search results to the API caller.
 */
@Data
@Builder
public class SearchResultDTO {
    /** Number of total search results */
    @JsonProperty("numResults")
    @Schema(description = "Number of total results (without pagination)", example = "801")
    private int numResults;

    /** Starting index within the total search results */
    @JsonProperty("startIndex")
    @Schema(description = "Start index within the total number of search results", example = "100")
    private int startIndex;

    /** List of books contained in the search result */
    @JsonProperty("searchResults")
    //@Schema(description = "Book DTOs of search results")
    private List<BookDTO> searchResults;

    /**
     * Convert a Search Result object to a Search Result DTO object
     * @param searchResult Search Result object to convert
     * @return Converted Search Result DTO object
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
