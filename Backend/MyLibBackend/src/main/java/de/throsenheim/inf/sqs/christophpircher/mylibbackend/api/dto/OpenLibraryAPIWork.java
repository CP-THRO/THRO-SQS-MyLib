package de.throsenheim.inf.sqs.christophpircher.mylibbackend.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;


/**
 *  Class to parse the description from the works API for a work with Jackson.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true) // To ignore everything not explicitly specified
public class OpenLibraryAPIWork {

    @JsonProperty("description")
    private Description description = new Description();

    /**
     * List of authors
     */
    @JsonProperty("authors")
    private List<Author> authors = new ArrayList<>();

    @Data
    @JsonDeserialize(using = DescriptionDeserializer.class)
    public static class Description{

        /**
         * Type. Always "/type/text"
         */
        @JsonProperty("type")
        private String type;

        /**
         * Actual text of the description. This is what I want.
         */
        @JsonProperty("value")
        private String value;
    }


    /**
     * Helper class to parse author object, which is kinda weird: { author: {key: "/authors/OL26320A"}, type: {key: "/type/author_role"} }
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Author{
        @JsonProperty("author")
        private AuthorKey authorKey;
    }


    /**
     * Helper class to parse the author keys.
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true) // To ignore everything not explicitly specified
    public static class AuthorKey{
        /** Key String */
        @JsonProperty("key")
        private String key;

        /** Parse out the actual key since the returned format is /authors/[key]  */
        public String getKeyWithoutURL(){
            return key.replace("/authors/", "");
        }
    }

}
