package de.throsenheim.inf.sqs.christophpircher.mylibbackend.dto;

import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.Book;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BookDTOTest {

    @Test
    void testFromBookMapsAllFieldsCorrectly() {

        Book book = new Book();
        book.setBookID("OL1234M");
        book.setTitle("Test Title");
        book.setSubtitle("Test Subtitle");
        book.setAuthors(List.of("Author A", "Author B"));
        book.setDescription("A great book");
        book.setIsbns(List.of("9781234567890"));
        book.setPublishDate("2020-01-01");
        book.setCoverURLSmall("small.jpg");
        book.setCoverURLMedium("medium.jpg");
        book.setCoverURLLarge("large.jpg");

        BookDTO dto = BookDTO.fromBook(book);

        assertEquals("OL1234M", dto.getBookID());
        assertEquals("Test Title", dto.getTitle());
        assertEquals("Test Subtitle", dto.getSubtitle());
        assertEquals(List.of("Author A", "Author B"), dto.getAuthors());
        assertEquals("A great book", dto.getDescription());
        assertEquals(List.of("9781234567890"), dto.getIsbns());
        assertEquals("2020-01-01", dto.getPublishDate());
        assertEquals("small.jpg", dto.getCoverURLSmall());
        assertEquals("medium.jpg", dto.getCoverURLMedium());
        assertEquals("large.jpg", dto.getCoverURLLarge());


        assertEquals(0, dto.getIndividualRating());
        assertFalse(dto.isBookIsInLibrary());
        assertFalse(dto.isBookIsOnWishlist());
        assertNull(dto.getReadingStatus()); // because not set
    }
}