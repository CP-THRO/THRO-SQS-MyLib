package de.throsenheim.inf.sqs.christophpircher.mylibbackend.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Class to parse the response of the search API with Jackson
 */
@Data
public class OpenLibraryAPISearchResponse {

    /**
     * Number of entries found by the API
     */
    @JsonProperty("numFound")
    private int numFound;

    /**
     * Starting entry index, since it is paginated.
     */
    @JsonProperty("start")
    private int start;

    /**
     * Search result works returned by the search API.
     */
    @JsonProperty("docs")
    private List<OpenLibraryAPISearchWork> searchResults;

}
