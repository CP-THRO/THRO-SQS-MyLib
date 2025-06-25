package de.throsenheim.inf.sqs.christophpircher.mylibbackend.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object (DTO) for parsing the response from the OpenLibrary Editions API.
 * <p>
 * This class is used specifically when a work does not have a {@code cover_edition_key},
 * allowing the application to fall back on other editions associated with the work.
 * </p>
 *
 * <p>Jackson is configured to ignore any unexpected or unused fields in the API response.</p>
 *
 * <p>Example JSON structure handled:</p>
 * <pre>
 * {
 *   "entries": [
 *     { "key": "/books/OL12345M" },
 *     { "key": "/books/OL67890M" }
 *   ]
 * }
 * </pre>
 *
 * @see OpenLibraryAPIEditions.Edition
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true) // To ignore everything not explicitly specified
public class OpenLibraryAPIEditions {
    /**
     * List of book edition entries returned by the API.
     * <p>Typically, only the first entry with a valid key is used.</p>
     */
    @JsonProperty("entries")
    List<Edition> editions = new ArrayList<>();

    /**
     * Inner class representing a single book edition entry.
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true) // To ignore everything not explicitly specified
    public static class Edition{

        /**
         * The full OpenLibrary key for this edition (e.g., {@code "/books/OL12345M"}).
         */
        @JsonProperty("key")
        private String bookKey;

        /**
         * Extracts the edition key by removing the {@code "/books/"} prefix from the full key.
         *
         * @return the book key without the "/books/" prefix
         */
        public String getBookKeyWithoutURL(){
            return bookKey.replace("/books/", "");
        }
    }
}
