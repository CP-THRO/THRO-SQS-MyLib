package de.throsenheim.inf.sqs.christophpircher.mylibbackend.api.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenLibraryAPIWorkTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testAuthorKeyGetKeyWithoutURL() {
        OpenLibraryAPIWork.AuthorKey key = new OpenLibraryAPIWork.AuthorKey();
        key.setKey("/authors/OL12345A");

        assertEquals("OL12345A", key.getKeyWithoutURL());
    }

    @Test
    void testDeserializeDescriptionAsText() throws JsonProcessingException {
        String json = """
            {
              "description": "A simple description"
            }
            """;

        OpenLibraryAPIWork result = objectMapper.readValue(json, OpenLibraryAPIWork.class);
        assertNotNull(result.getDescription());
        assertEquals("A simple description", result.getDescription().getValue());
        assertNull(result.getDescription().getType());
    }

    @Test
    void testDeserializeDescriptionAsObject() throws JsonProcessingException {
        String json = """
            {
              "description": {
                "value": "A structured description",
                "type": "text"
              }
            }
            """;

        OpenLibraryAPIWork result = objectMapper.readValue(json, OpenLibraryAPIWork.class);
        assertNotNull(result.getDescription());
        assertEquals("A structured description", result.getDescription().getValue());
        assertEquals("text", result.getDescription().getType());
    }

    @Test
    void testDeserializeAuthors() throws JsonProcessingException {
        String json = """
            {
              "authors": [
                {
                  "author": {
                    "key": "/authors/OL54321A"
                  }
                }
              ]
            }
            """;

        OpenLibraryAPIWork result = objectMapper.readValue(json, OpenLibraryAPIWork.class);
        assertNotNull(result.getAuthors());
        assertEquals(1, result.getAuthors().size());

        OpenLibraryAPIWork.Author author = result.getAuthors().getFirst();
        assertNotNull(author.getAuthorKey());
        assertEquals("/authors/OL54321A", author.getAuthorKey().getKey());
        assertEquals("OL54321A", author.getAuthorKey().getKeyWithoutURL());
    }
}
