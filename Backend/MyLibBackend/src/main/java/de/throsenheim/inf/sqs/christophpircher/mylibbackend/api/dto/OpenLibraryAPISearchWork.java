package de.throsenheim.inf.sqs.christophpircher.mylibbackend.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Data Transfer Object (DTO) for parsing individual work entries from the OpenLibrary Search API.
 * <p>
 * This class captures the key fields returned for each search result ("work") such as title,
 * authors, cover information, and publication details. It is typically used as part of a list
 * in {@link OpenLibraryAPISearchResponse}.
 * </p>
 *
 * <p>Example fields parsed:</p>
 * <pre>
 * {
 *   "key": "/works/OL12345W",
 *   "title": "Example Book",
 *   "cover_edition_key": "OL67890M",
 *   "cover_i": 123456,
 *   "author_name": ["Author One", "Author Two"],
 *   "first_publish_year": 1999
 * }
 * </pre>
 *
 * <p>Unknown fields in the API response are ignored due to {@code @JsonIgnoreProperties}.</p>
 *
 * @see OpenLibraryAPISearchResponse
 * @see com.fasterxml.jackson.annotation.JsonProperty
 * @see com.fasterxml.jackson.databind.ObjectMapper
 *
 * Author:
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true) // To ignore everything not explicitly specified
public class OpenLibraryAPISearchWork {

    /**
     * The OpenLibrary work key (e.g., {@code "/works/OL12345W"}).
     * <p>Used as a fallback to retrieve additional book data.</p>
     */
    @JsonProperty("key")
    private String workKey;

    /**
     * The title of the book.
     */
    @JsonProperty("title")
    private String title;

    /**
     * Subtitle of the book
     */
    @JsonProperty("subtitle")
    private String subtitle;

    /**
     * The cover edition key, used to identify the edition representing the work.
     * <p>Helpful when rendering a representative cover image.</p>
     */
    @JsonProperty("cover_edition_key")
    private String coverEditionKey;

    /**
     * The numeric ID of the cover image, used with OpenLibrary's image API.
     */
    @JsonProperty("cover_i")
    private int coverID;

    /**
     * List of author names for the work.
     */
    @JsonProperty("author_name")
    private List<String> authors;

    /**
     * The year the book was first published.
     */
    @JsonProperty("first_publish_year")
    private int firstPublishYear;


    /**
     * Extracts the raw work key by removing the "/works/" prefix.
     *
     * @return the work key without the "/works/" prefix
     */
    public String getWorkKeyWithoutURL(){
        return workKey.replace("/works/","");
    }

}
