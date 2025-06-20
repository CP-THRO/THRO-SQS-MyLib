package de.throsenheim.inf.sqs.christophpircher.mylibbackend.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;


/**
 * Data Transfer Object (DTO) for parsing a work object from the OpenLibrary Works API.
 * <p>
 * This class handles the retrieval of descriptive metadata and authorship information
 * for a given work. It uses Jackson to deserialize nested and sometimes inconsistent
 * structures in the API.
 * </p>
 *
 * <p>Example fields handled:</p>
 * <pre>
 * {
 *   "description": "A great book",
 *   "authors": [
 *     { "author": { "key": "/authors/OL12345A" }, "type": { "key": "/type/author_role" } }
 *   ]
 * }
 * </pre>
 *
 * <p>Unknown fields are ignored via {@code @JsonIgnoreProperties}.</p>
 *
 * @see DescriptionDeserializer
 * @see com.fasterxml.jackson.databind.ObjectMapper
 *
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true) // To ignore everything not explicitly specified
public class OpenLibraryAPIWork {

    /**
     * The description of the work, which may be a simple string or a structured object.
     * <p>Deserialized using a custom {@link DescriptionDeserializer}.</p>
     */
    @JsonProperty("description")
    private Description description = new Description();

    /**
     * List of authors associated with the work.
     */
    @JsonProperty("authors")
    private List<Author> authors = new ArrayList<>();

    /**
     * Inner class representing the work's description.
     * <p>This class supports both simple string and structured formats.</p>
     */
    @Data
    @JsonDeserialize(using = DescriptionDeserializer.class)
    public static class Description{


        /**
         * The type of the description (e.g., {@code "/type/text"}).
         */
        @JsonProperty("type")
        private String type;

        /**
         * The actual descriptive text of the work.
         */
        @JsonProperty("value")
        private String value;
    }


    /**
     * Inner class representing an author entry from the work.
     * <p>Each author object wraps an {@link AuthorKey} instance.</p>
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Author{
        /**
         * The OpenLibrary author key (e.g., {@code "/authors/OL12345A"}).
         */
        @JsonProperty("author")
        private AuthorKey authorKey;
    }


    /**
     * Inner class representing the author key structure.
     * <p>Used to extract a clean author identifier string.</p>
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true) // To ignore everything not explicitly specified
    public static class AuthorKey{
        /**
         * The full author key (e.g., {@code "/authors/OL12345A"}).
         */
        @JsonProperty("key")
        private String key;

        /**
         * Removes the {@code "/authors/"} prefix to extract the actual author ID.
         *
         * @return the author ID without the "/authors/" prefix
         */
        public String getKeyWithoutURL(){
            return key.replace("/authors/", "");
        }
    }

}
