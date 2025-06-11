package de.throsenheim.inf.sqs.christophpircher.mylibbackend.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * This class contains is used to automatically parse the relevant fields for the search api results with Jackson
 */
@Data
public class OpenLibraryAPISearchWork {

    /**
     * Key of the "Cover edition": The edition which represents all editions. This will be shown.
     * Everything else will be fetched from this.
     * In theory, all search results contain part of the information. But since I need to call the Books API anyway for the ISBN, and I need that API for storing books in a personal library, I am getting everything from there.
     *
     */
    @JsonProperty("cover_edition_key")
    private String coverEditionKey;

}
