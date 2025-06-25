package de.throsenheim.inf.sqs.christophpircher.mylibbackend.dto;

import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.Book;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.BookList;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BookListDTOTest {

    @Test
    void testFromSearchResultMapsCorrectly() {
        // Given
        Book book1 = Book.builder()
                .bookID("OL123")
                .title("Book One")
                .build();

        Book book2 = Book.builder()
                .bookID("OL456")
                .title("Book Two")
                .build();

        BookList bookList = BookList.builder()
                .numResults(2)
                .startIndex(0)
                .skippedBooks(1)
                .books(List.of(book1, book2))
                .build();

        // When
        BookListDTO dto = BookListDTO.fromSearchResult(bookList);

        // Then
        assertEquals(2, dto.getNumResults());
        assertEquals(0, dto.getStartIndex());
        assertEquals(1, dto.getSkippedBooks());

        assertNotNull(dto.getBooks());
        assertEquals(2, dto.getBooks().size());
        assertEquals("OL123", dto.getBooks().get(0).getBookID());
        assertEquals("Book One", dto.getBooks().get(0).getTitle());
        assertEquals("OL456", dto.getBooks().get(1).getBookID());
    }
}