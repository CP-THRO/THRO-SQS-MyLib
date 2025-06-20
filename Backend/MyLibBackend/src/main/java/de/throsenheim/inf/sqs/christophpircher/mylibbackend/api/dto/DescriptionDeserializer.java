package de.throsenheim.inf.sqs.christophpircher.mylibbackend.api.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import java.io.IOException;


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
public class DescriptionDeserializer extends JsonDeserializer<OpenLibraryAPIWork.Description> {

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