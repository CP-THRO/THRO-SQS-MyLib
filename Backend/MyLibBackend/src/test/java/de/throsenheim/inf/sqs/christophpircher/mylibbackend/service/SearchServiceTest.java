package de.throsenheim.inf.sqs.christophpircher.mylibbackend.service;

import de.throsenheim.inf.sqs.christophpircher.mylibbackend.api.OpenLibraryAPI;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.exceptions.UnexpectedStatusException;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.Book;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.BookList;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.service.flyweights.SearchResultFlyweightFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    private OpenLibraryAPI openLibraryAPI;

    @Mock
    private SearchResultFlyweightFactory flyweightFactory;

    @InjectMocks
    private SearchService searchService;

    private static final String VALID_KEYWORDS = "java";
    private static final String EMPTY_KEYWORDS = "   ";
    private static final int START_INDEX = 0;
    private static final int NUM_RESULTS = 5;

    private BookList mockBookList;

    @BeforeEach
    void setUp() {
        mockBookList = BookList.builder()
                .books(List.of(Book.builder().bookID("OL123").title("Effective Java").build()))
                .numResults(1)
                .startIndex(0)
                .skippedBooks(0)
                .build();
    }

    @Test
    void searchKeywordsExternal_shouldReturnResults_whenKeywordsAreValid() throws UnexpectedStatusException, IOException {
        when(flyweightFactory.search(VALID_KEYWORDS, START_INDEX, NUM_RESULTS)).thenReturn(mockBookList);

        BookList result = searchService.searchKeywordsExternal(VALID_KEYWORDS, START_INDEX, NUM_RESULTS);

        assertEquals(1, result.getBooks().size());
        verify(flyweightFactory).search(VALID_KEYWORDS, START_INDEX, NUM_RESULTS);
    }

    @Test
    void searchKeywordsExternal_shouldLogWarning_whenKeywordsAreBlank() throws UnexpectedStatusException, IOException {
        when(flyweightFactory.search(EMPTY_KEYWORDS, START_INDEX, NUM_RESULTS)).thenReturn(mockBookList);

        BookList result = searchService.searchKeywordsExternal(EMPTY_KEYWORDS, START_INDEX, NUM_RESULTS);

        assertEquals(1, result.getBooks().size());
        verify(flyweightFactory).search(EMPTY_KEYWORDS, START_INDEX, NUM_RESULTS);
    }

    @Test
    void searchKeywordsExternal_shouldLogWarning_whenKeywordsAreNull() throws UnexpectedStatusException, IOException {
        when(flyweightFactory.search(null, START_INDEX, NUM_RESULTS)).thenReturn(mockBookList);

        BookList result = searchService.searchKeywordsExternal(null, START_INDEX, NUM_RESULTS);

        assertEquals(1, result.getBooks().size());
        verify(flyweightFactory).search(null, START_INDEX, NUM_RESULTS);
    }

    @Test
    void searchKeywordsExternal_shouldThrowException_whenFactoryThrowsIOException() throws UnexpectedStatusException, IOException {
        when(flyweightFactory.search(VALID_KEYWORDS, START_INDEX, NUM_RESULTS))
                .thenThrow(new IOException("Connection failed"));

        assertThrows(IOException.class, () ->
                searchService.searchKeywordsExternal(VALID_KEYWORDS, START_INDEX, NUM_RESULTS));
    }

    @Test
    void searchKeywordsExternal_shouldThrowException_whenFactoryThrowsUnexpectedStatusException() throws UnexpectedStatusException, IOException {
        when(flyweightFactory.search(VALID_KEYWORDS, START_INDEX, NUM_RESULTS))
                .thenThrow(new UnexpectedStatusException("Unexpected response"));

        assertThrows(UnexpectedStatusException.class, () ->
                searchService.searchKeywordsExternal(VALID_KEYWORDS, START_INDEX, NUM_RESULTS));
    }
}
