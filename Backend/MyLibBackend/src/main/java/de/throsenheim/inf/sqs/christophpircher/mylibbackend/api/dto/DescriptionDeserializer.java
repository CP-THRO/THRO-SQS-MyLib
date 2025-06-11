package de.throsenheim.inf.sqs.christophpircher.mylibbackend.api.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import java.io.IOException;

/**
 * Deserializer for the description in work, since it can be just a string OR an object...
 */
public class DescriptionDeserializer extends JsonDeserializer<OpenLibraryAPIWork.Description> {

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