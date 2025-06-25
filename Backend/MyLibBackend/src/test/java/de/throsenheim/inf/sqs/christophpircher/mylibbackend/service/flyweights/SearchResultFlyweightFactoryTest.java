package de.throsenheim.inf.sqs.christophpircher.mylibbackend.service.flyweights;

import de.throsenheim.inf.sqs.christophpircher.mylibbackend.api.OpenLibraryAPI;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.exceptions.UnexpectedStatusException;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.Book;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.BookList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchResultFlyweightFactoryTest {

    @Mock
    private OpenLibraryAPI openLibraryAPI;

    @InjectMocks
    private SearchResultFlyweightFactory flyweightFactory;

    private static final String KEYWORDS = "java";
    private static final int START = 0;
    private static final int COUNT = 5;

    private static final BookList MOCK_BOOKLIST = BookList.builder()
            .books(List.of(Book.builder().bookID("OL123").title("Effective Java").build()))
            .numResults(1)
            .startIndex(0)
            .skippedBooks(0)
            .build();

    private Field cacheField;

    @BeforeEach
    void setUp() throws Exception {
        cacheField = SearchResultFlyweightFactory.class.getDeclaredField("bookListCache");
        cacheField.setAccessible(true);
        getCache().clear();
    }

    @Test
    void searchShouldReturnCachedResultWhenCacheIsValid() throws IllegalAccessException, UnexpectedStatusException, IOException {
        SearchResultFlyweightFactory.SearchResultFlyweightKey key =
                new SearchResultFlyweightFactory.SearchResultFlyweightKey(KEYWORDS, START, COUNT);
        CacheEntry<BookList> entry = new CacheEntry<>(MOCK_BOOKLIST);

        getCache().put(key, entry);

        BookList result = flyweightFactory.search(KEYWORDS, START, COUNT);

        assertEquals(MOCK_BOOKLIST, result);
        verify(openLibraryAPI, never()).searchBooks(any(), anyInt(), anyInt());
    }

    @Test
    void searchShouldFetchFromAPIWhenCacheEntryExpired() throws NoSuchFieldException, IllegalAccessException, UnexpectedStatusException, IOException {
        SearchResultFlyweightFactory.SearchResultFlyweightKey key =
                new SearchResultFlyweightFactory.SearchResultFlyweightKey(KEYWORDS, START, COUNT);

        // Use real entry but manipulate timestamp via reflection
        CacheEntry<BookList> expiredEntry = new CacheEntry<>(MOCK_BOOKLIST);
        Field timestampField = CacheEntry.class.getDeclaredField("timestamp");
        timestampField.setAccessible(true);
        timestampField.setLong(expiredEntry, System.currentTimeMillis() - (61L * 60 * 1000)); // force expired

        getCache().put(key, expiredEntry);
        when(openLibraryAPI.searchBooks(KEYWORDS, START, COUNT)).thenReturn(MOCK_BOOKLIST);

        BookList result = flyweightFactory.search(KEYWORDS, START, COUNT);

        assertEquals(MOCK_BOOKLIST, result);
        assertTrue(getCache().containsKey(key));
    }

    @Test
    void searchShouldFetchFromAPIWhenCacheMiss() throws Exception {
        when(openLibraryAPI.searchBooks(KEYWORDS, START, COUNT)).thenReturn(MOCK_BOOKLIST);

        BookList result = flyweightFactory.search(KEYWORDS, START, COUNT);

        assertEquals(MOCK_BOOKLIST, result);
    }

    @Test
    void cleanupCacheShouldOnlyRemoveExpiredEntries() throws Exception {
        long now = System.currentTimeMillis();

        SearchResultFlyweightFactory.SearchResultFlyweightKey keyKeep =
                new SearchResultFlyweightFactory.SearchResultFlyweightKey("keep", 0, 1);
        SearchResultFlyweightFactory.SearchResultFlyweightKey keyDrop =
                new SearchResultFlyweightFactory.SearchResultFlyweightKey("drop", 1, 1);

        CacheEntry<BookList> freshEntry = new CacheEntry<>(MOCK_BOOKLIST);
        CacheEntry<BookList> expiredEntry = new CacheEntry<>(MOCK_BOOKLIST);

        // Force expiration of one entry via reflection
        Field timestampField = CacheEntry.class.getDeclaredField("timestamp");
        timestampField.setAccessible(true);
        timestampField.setLong(expiredEntry, now - (61L * 60 * 1000));

        getCache().put(keyKeep, freshEntry);
        getCache().put(keyDrop, expiredEntry);

        invokeCleanupCache();

        assertTrue(getCache().containsKey(keyKeep));
        assertFalse(getCache().containsKey(keyDrop));
    }

    private void invokeCleanupCache() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        java.lang.reflect.Method method = SearchResultFlyweightFactory.class.getDeclaredMethod("cleanupCache");
        method.setAccessible(true);
        method.invoke(flyweightFactory);
    }

    @SuppressWarnings("unchecked")
    private ConcurrentHashMap<SearchResultFlyweightFactory.SearchResultFlyweightKey, CacheEntry<BookList>> getCache()
            throws IllegalAccessException {
        return (ConcurrentHashMap<SearchResultFlyweightFactory.SearchResultFlyweightKey, CacheEntry<BookList>>) cacheField.get(flyweightFactory);
    }
}
