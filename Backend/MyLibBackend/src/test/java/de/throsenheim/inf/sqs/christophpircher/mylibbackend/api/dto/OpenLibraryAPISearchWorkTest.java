package de.throsenheim.inf.sqs.christophpircher.mylibbackend.api.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenLibraryAPISearchWorkTest {
    private static final String GENERIC_WORK_ID = "OL123456W";
    private static final String GENERIC_WORK_ID_UNPARSED = "/works/OL123456W";

    @Test
    void testGetWorkKeyWithoutURLWithPrefix() {
        OpenLibraryAPISearchWork work = new OpenLibraryAPISearchWork();
        work.setWorkKey("GENERIC_WORK_ID_UNPARSED");

        assertEquals(GENERIC_WORK_ID, work.getWorkKeyWithoutURL());
    }

    @Test
    void testGetWorkKeyWithoutURLWithoutPrefix() {
        OpenLibraryAPISearchWork work = new OpenLibraryAPISearchWork();
        work.setWorkKey(GENERIC_WORK_ID);

        assertEquals(GENERIC_WORK_ID_UNPARSED, work.getWorkKeyWithoutURL());
    }

    @Test
    void testGetWorkKeyWithoutURLNullWorkKey() {
        OpenLibraryAPISearchWork work = new OpenLibraryAPISearchWork();
        work.setWorkKey(null);

        assertThrows(NullPointerException.class, work::getWorkKeyWithoutURL);
    }
}