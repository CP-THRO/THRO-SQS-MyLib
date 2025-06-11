package de.throsenheim.inf.sqs.christophpircher.mylibbackend.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * This class contains is used to automatically parse the relevant fields for the search api results with Jackson
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true) // To ignore everything not explicitly specified
public class OpenLibraryAPISearchWork {

    /**
     * Key of the work, as a backup option to get a book key.
     */
    @JsonProperty("key")
    private String workKey;

    /**
     * The title of the book
     */
    @JsonProperty("title")
    private String title;

    /**
     * Key of the "Cover edition": The edition which represents all editions. This will be shown.
     *
     */
    @JsonProperty("cover_edition_key")
    private String coverEditionKey;

    /**
     * ID of the cover
     */
    @JsonProperty("cover_i")
    private int coverID;

    /**
     * List of authors of the book
     */
    @JsonProperty("author_name")
    private List<String> authors;

    /**
     * Year when the book was first published
     */
    @JsonProperty("first_publish_year")
    private int firstPublishYear;


    /**
     * Remove the "/works/" part from the work key
     * @return Work key without "/works/"
     */
    public String getWorkKeyWithoutURL(){
        return workKey.replace("/works/","");
    }

}
