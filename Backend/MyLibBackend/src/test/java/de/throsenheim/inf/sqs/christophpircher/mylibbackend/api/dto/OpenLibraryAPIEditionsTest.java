package de.throsenheim.inf.sqs.christophpircher.mylibbackend.api.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenLibraryAPIEditionsTest {

    private static final String GENERIC_BOOK_ID = "OL123456M";
    private static final String GENERIC_BOOK_ID_UNPARSED = "/books/OL123456M";

    @Test
    void testGetBookKeyWithoutURLWithPrefix() {
        OpenLibraryAPIEditions.Edition edition = new OpenLibraryAPIEditions.Edition();
        edition.setBookKey(GENERIC_BOOK_ID_UNPARSED);

        String result = edition.getBookKeyWithoutURL();
        assertEquals(GENERIC_BOOK_ID, result);
    }

    @Test
    void testGetBookKeyWithoutURLWithoutPrefix() {
        OpenLibraryAPIEditions.Edition edition = new OpenLibraryAPIEditions.Edition();
        edition.setBookKey(GENERIC_BOOK_ID);

        String result = edition.getBookKeyWithoutURL();
        assertEquals(GENERIC_BOOK_ID, result); // No change expected
    }

    @Test
    void testGetBookKeyWithoutURLNullKeyShouldThrowNPE() {
        OpenLibraryAPIEditions.Edition edition = new OpenLibraryAPIEditions.Edition();
        edition.setBookKey(null);

        assertThrows(NullPointerException.class, edition::getBookKeyWithoutURL);
    }
}
