package de.throsenheim.inf.sqs.christophpircher.mylibbackend.api.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenLibraryAPIEditionsTest {

    @Test
    void testGetBookKeyWithoutURL_withPrefix() {
        OpenLibraryAPIEditions.Edition edition = new OpenLibraryAPIEditions.Edition();
        edition.setBookKey("/books/OL99999M");

        String result = edition.getBookKeyWithoutURL();
        assertEquals("OL99999M", result);
    }

    @Test
    void testGetBookKeyWithoutURL_withoutPrefix() {
        OpenLibraryAPIEditions.Edition edition = new OpenLibraryAPIEditions.Edition();
        edition.setBookKey("OL99999M");

        String result = edition.getBookKeyWithoutURL();
        assertEquals("OL99999M", result); // No change expected
    }

    @Test
    void testGetBookKeyWithoutURL_nullKey_shouldThrowNPE() {
        OpenLibraryAPIEditions.Edition edition = new OpenLibraryAPIEditions.Edition();
        edition.setBookKey(null);

        assertThrows(NullPointerException.class, edition::getBookKeyWithoutURL);
    }
}
