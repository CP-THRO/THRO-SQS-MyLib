package de.throsenheim.inf.sqs.christophpircher.mylibbackend.api.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenLibraryAPISearchWorkTest {

    @Test
    void testGetWorkKeyWithoutURL_withPrefix() {
        OpenLibraryAPISearchWork work = new OpenLibraryAPISearchWork();
        work.setWorkKey("/works/OL12345W");

        assertEquals("OL12345W", work.getWorkKeyWithoutURL());
    }

    @Test
    void testGetWorkKeyWithoutURL_withoutPrefix() {
        OpenLibraryAPISearchWork work = new OpenLibraryAPISearchWork();
        work.setWorkKey("OL12345W");

        assertEquals("OL12345W", work.getWorkKeyWithoutURL());
    }

    @Test
    void testGetWorkKeyWithoutURL_nullWorkKey() {
        OpenLibraryAPISearchWork work = new OpenLibraryAPISearchWork();
        work.setWorkKey(null);

        assertThrows(NullPointerException.class, work::getWorkKeyWithoutURL);
    }
}