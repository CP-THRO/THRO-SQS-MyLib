package de.throsenheim.inf.sqs.christophpircher.mylibbackend.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object (DTO) for parsing book information from the OpenLibrary Books API.
 * <p>
 * This class is primarily used to extract:
 * <ul>
 *     <li>ISBN-10 and ISBN-13 identifiers</li>
 *     <li>Book title and subtitle</li>
 *     <li>Cover image IDs</li>
 *     <li>Publish date</li>
 *     <li>Associated work IDs</li>
 * </ul>
 * It includes utility methods to simplify access to clean book and work identifiers.
 * </p>
 *
 * <p>Jackson will ignore any unexpected fields thanks to {@code @JsonIgnoreProperties}.</p>
 *
 * @see OpenLibraryAPIBook.WorkKey
 * @see com.fasterxml.jackson.databind.ObjectMapper
 * @see com.fasterxml.jackson.annotation.JsonProperty
 *
 * Author:
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true) // To ignore everything not explicitly specified
public class OpenLibraryAPIBook {

    /**
     * List of ISBN-10 identifiers for the book.
     * <p>Some books may not have ISBN-10 numbers.</p>
     */
    @JsonProperty("isbn_10")
    private List<String> isbn10s = new ArrayList<>(); //default value so I don't have to do a null check if there are no isbn10s. Some books don't have an isbn10.

    /**
     * List of ISBN-13 identifiers for the book.
     * <p>Some books may not have ISBN-13 numbers.</p>
     */
    @JsonProperty("isbn_13")
    private List<String> isbn13s  = new ArrayList<>(); //default value so I don't have to do a null check if there are no isbn13s. Some books don't have an isbn13.

    /**
     * The full OpenLibrary key for the book (e.g., {@code "/books/OL12345M"}).
     */
    @JsonProperty("key")
    private String bookID;

    /**
     * List of cover image IDs for the book.
     * <p>Typically only the first ID is used for display purposes.</p>
     */
    @JsonProperty("covers")
    private List<Integer> coverIDs = new ArrayList<>(); // default value so I don't have to do a null check if there are no covers. Some books don't have a cover.

    /**
     * The book's title.
     */
    @JsonProperty("title")
    private String title;

    /**
     * The book's subtitle (if available).
     */
    @JsonProperty("subtitle")
    private String subtitle;

    /**
     * The publication date of the book
     */
    @JsonProperty("publish_date")
    private String publishDate;

    /**
     * List of associated work keys (e.g., {@code "/works/OL123456W"}).
     * <p>Typically only the first entry is relevant.</p>
     */
    @JsonProperty("works")
    private  List<WorkKey> workKeys;


    /**
     * Extracts the book ID by removing the {@code "/books/"} prefix from the full key.
     *
     * @return the book ID without the "/books/" prefix
     */
    public String getBookIDWithoutURL(){
        return bookID.replace("/books/", "");
    }

    /**
     * Inner class used to deserialize work key references from the OpenLibrary API.
     */
    @Data
    public static class WorkKey{
        /**
         * Full OpenLibrary work key (e.g., {@code "/works/OL123456W"}).
         */
        @JsonProperty("key")
        private String key;

        /**
         * Extracts the work ID by removing the {@code "/works/"} prefix from the full key.
         *
         * @return the work ID without the "/works/" prefix
         */
        public String getKeyWithoutURL(){
            return key.replace("/works/", "");
        }
    }
}
