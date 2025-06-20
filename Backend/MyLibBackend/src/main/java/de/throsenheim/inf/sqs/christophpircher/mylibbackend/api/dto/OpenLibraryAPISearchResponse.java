package de.throsenheim.inf.sqs.christophpircher.mylibbackend.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Data Transfer Object (DTO) for parsing the response from the OpenLibrary Search API.
 * <p>
 * This class is deserialized using Jackson and captures relevant search metadata
 * and a list of matched works. It is used in responses to keyword searches
 * </p>
 *
 * <p>Example API response fields handled by this class:</p>
 * <pre>
 * {
 *   "numFound": 4321,
 *   "start": 0,
 *   "docs": [ {...}, {...} ]
 * }
 * </pre>
 *
 * <p>All unspecified fields from the API are ignored due to {@code @JsonIgnoreProperties}.</p>
 *
 * @see OpenLibraryAPISearchWork
 * @see com.fasterxml.jackson.databind.ObjectMapper
 * @see com.fasterxml.jackson.annotation.JsonProperty
 *
 * Author:
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true) // To ignore everything not explicitly specified
public class OpenLibraryAPISearchResponse {

    /**
     * Total number of works found by the search query.
     */
    @JsonProperty("numFound")
    private int numFound;

    /**
     * The starting index of the current page of results.
     * <p>Useful for paginated responses.</p>
     */
    @JsonProperty("start")
    private int start;

    /**
     * List of search result entries (works) returned by the OpenLibrary Search API.
     *
     * @see OpenLibraryAPISearchWork
     */
    @JsonProperty("docs")
    private List<OpenLibraryAPISearchWork> searchResults;

}
