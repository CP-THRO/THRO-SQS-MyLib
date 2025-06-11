package de.throsenheim.inf.sqs.christophpircher.mylibbackend.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Class to parse the necessary information from the books API of a book. Mainly used to get the information about the cover book.
 * Mainly the ISBN10/ISBN13 numbers.
 */
@Data
public class OpenLibraryAPIBook {

    /**
     * List of ISBN 10 numbers
     */
    @JsonProperty("isbn_10")
    private List<String> isbn10s;

    /**
     * List of ISBN 13 numbers
     */
    @JsonProperty("isbn_13")
    private List<String> isbn13s;

    /**
     * OpenLibrary Book ID / key.
     */
    @JsonProperty("key")
    private String bookID;

    /**
     * List of cover IDs. I will use the first entry only.
     */
    @JsonProperty("cover_i")
    private List<Integer> coverIDs;

    /**
     * The title of the book
     */
    @JsonProperty("title")
    private String title;

    /**
     * Subtitle of the book
     */
    @JsonProperty("subtitle")
    private String subtitle;

    /**
     * Year it was published
     */
    @JsonProperty("publish_date")
    private String publishDate;

    /**
     * List which contains all the authors.
     */
    @JsonProperty("author_name")
    private List<AuthorKey> authorKeys;

    /**
     * List of work keys. I will always use just the first entry.
     */
    @JsonProperty("works")
    private  List<WorkKey> workKeys;


    /**
     * Parse out the actual ID/key since the format returned is /books/[key]
     * @return The key without "/books/".
     */
    public String getBookIDWithoutURL(){
        return bookID.replace("/books/", "");
    }

    /**
     * Helper class to parse the author keys.
     */
    @Data
    public static class AuthorKey{
        /** Key String */
        @JsonProperty("key")
        private String key;

        /** Parse out the actual key since the returned format is /authors/[key]  */
        public String getKeyWithoutURL(){
            return key.replace("/authors/", "");
        }
    }

    /**
     * Helper class to parse the work keys
     */
    @Data
    public static class WorkKey{
        /** Key String */
        @JsonProperty("key")
        private String key;

        /** Parse out the actual key since the returned format is /works/[key]  */
        public String getKeyWithoutURL(){
            return key.replace("/works/", "");
        }
    }
}
