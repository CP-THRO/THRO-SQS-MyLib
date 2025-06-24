package de.throsenheim.inf.sqs.christophpircher.mylibbackend.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.io.IOException;
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

    /**
     * Custom Jackson deserializer for the {@link OpenLibraryAPIWork.Description} class.
     * <p>
     * The OpenLibrary API returns the "description" field in two possible formats:
     * </p>
     * <ul>
     *     <li>As a plain string (e.g., {@code "A simple description"})</li>
     *     <li>As an object with "value" and optionally "type" fields
     *         (e.g., {@code {"value": "A detailed description", "type": "text"}})</li>
     * </ul>
     * <p>
     * This deserializer handles both formats and converts them into a unified
     * {@link OpenLibraryAPIWork.Description} object.
     * </p>
     *
     * <p>Usage: This is typically used by Jackson during JSON deserialization when reading OpenLibrary API responses.</p>
     *
     * @see OpenLibraryAPIWork.Description
     * @see JsonDeserializer
     * @see JsonParser
     * @see ObjectMapper
     */
    private static class DescriptionDeserializer extends JsonDeserializer<OpenLibraryAPIWork.Description> {

        /**
         * Deserializes the "description" field from JSON into a {@link OpenLibraryAPIWork.Description} object.
         * <p>
         * Supports both string and object representations of the field.
         * </p>
         *
         * @param parser the Jackson parser
         * @param context the deserialization context
         * @return the populated {@link OpenLibraryAPIWork.Description} object
         * @throws IOException if a parsing error occurs
         */

        @Override
        public OpenLibraryAPIWork.Description deserialize(JsonParser parser, DeserializationContext context)
                throws IOException {

            JsonNode node = parser.getCodec().readTree(parser);
            OpenLibraryAPIWork.Description description = new OpenLibraryAPIWork.Description();

            if (node.isTextual()) {
                // Description is just a plain string
                description.setValue(node.asText());
            } else if (node.isObject()) {
                // Description is a proper object
                JsonNode valueNode = node.get("value");
                if (valueNode != null && valueNode.isTextual()) {
                    description.setValue(valueNode.asText());
                }
                JsonNode typeNode = node.get("type");
                if (typeNode != null && typeNode.isTextual()) {
                    description.setType(typeNode.asText());
                }
            }

            return description;
        }
    }

}
