package de.throsenheim.inf.sqs.christophpircher.mylibbackend.api.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OpenLibraryAPIBookTest {

    @Test
    void testGetBookIDWithoutURL() {
        OpenLibraryAPIBook book = new OpenLibraryAPIBook();
        book.setBookID("/books/OL12345M");

        assertEquals("OL12345M", book.getBookIDWithoutURL());
    }

    @Test
    void testGetBookIDWithoutURL_whenNoPrefix_shouldReturnOriginal() {
        OpenLibraryAPIBook book = new OpenLibraryAPIBook();
        book.setBookID("OL12345M");

        assertEquals("OL12345M", book.getBookIDWithoutURL());
    }

    @Test
    void testGetKeyWithoutURL() {
        OpenLibraryAPIBook.WorkKey workKey = new OpenLibraryAPIBook.WorkKey();
        workKey.setKey("/works/OL54321W");

        assertEquals("OL54321W", workKey.getKeyWithoutURL());
    }

    @Test
    void testGetKeyWithoutURL_whenNoPrefix_shouldReturnOriginal() {
        OpenLibraryAPIBook.WorkKey workKey = new OpenLibraryAPIBook.WorkKey();
        workKey.setKey("OL54321W");

        assertEquals("OL54321W", workKey.getKeyWithoutURL());
    }

}