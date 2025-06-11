package de.throsenheim.inf.sqs.christophpircher.mylibbackend.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Class to parse the editions API response. This is used to get a book for a work that does not have cover, and therefore do not have a cover_edition_key field
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true) // To ignore everything not explicitly specified
public class OpenLibraryAPIEditions {
    /**
     * List of editions. I will search for the first that has a key.
     */
    @JsonProperty("entries")
    List<Edition> editions;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true) // To ignore everything not explicitly specified
    public static class Edition{

        @JsonProperty("key")
        private String bookKey;

        /**
         * Remove the URL part of the key
         * @return Key without "/books/"
         */
        public String getBookKeyWithoutURL(){
            return bookKey.replace("/books/", "");
        }
    }
}
