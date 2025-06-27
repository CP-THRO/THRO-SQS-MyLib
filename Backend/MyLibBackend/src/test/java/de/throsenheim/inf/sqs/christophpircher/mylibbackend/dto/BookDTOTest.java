package de.throsenheim.inf.sqs.christophpircher.mylibbackend.dto;

import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.Book;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BookDTOTest {

    @Test
    void testFromBookMapsAllFieldsCorrectly() {

        Book.BookBuilder builder = Book.builder();
        builder.bookID("OL1234M").title("Test Title").subtitle("Test Subtitle").authors(List.of("Author A", "Author B")).description("A great book").isbns(List.of("9781234567890")).publishDate("2020-01-01").coverURLSmall("small.jpg").coverURLMedium("medium.jpg").coverURLLarge("large.jpg");

        Book book = builder.build();
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