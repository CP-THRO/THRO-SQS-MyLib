package de.throsenheim.inf.sqs.christophpircher.mylibbackend.api.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenLibraryAPIBookTest {

    private static final String GENERIC_BOOK_ID = "OL123456M";
    private static final String GENERIC_BOOK_ID_UNPARSED = "/books/OL123456M";
    private static final String GENERIC_WORK_ID = "OL123456W";
    private static final String GENERIC_WORK_ID_UNPARSED = "/works/OL123456W";


    @Test
    void testGetBookIDWithoutURL() {
        OpenLibraryAPIBook book = new OpenLibraryAPIBook();
        book.setBookID(GENERIC_BOOK_ID_UNPARSED);

        assertEquals(GENERIC_BOOK_ID, book.getBookIDWithoutURL());
    }

    @Test
    void testGetBookIDWithoutURLWhenNoPrefixShouldReturnOriginal() {
        OpenLibraryAPIBook book = new OpenLibraryAPIBook();
        book.setBookID(GENERIC_BOOK_ID);

        assertEquals(GENERIC_BOOK_ID, book.getBookIDWithoutURL());
    }

    @Test
    void testGetKeyWithoutURL() {
        OpenLibraryAPIBook.WorkKey workKey = new OpenLibraryAPIBook.WorkKey();
        workKey.setKey(GENERIC_WORK_ID_UNPARSED);

        assertEquals(GENERIC_WORK_ID, workKey.getKeyWithoutURL());
    }

    @Test
    void testGetKeyWithoutURLWhenNoPrefixShouldReturnOriginal() {
        OpenLibraryAPIBook.WorkKey workKey = new OpenLibraryAPIBook.WorkKey();
        workKey.setKey(GENERIC_WORK_ID);

        assertEquals(GENERIC_WORK_ID, workKey.getKeyWithoutURL());
    }

}