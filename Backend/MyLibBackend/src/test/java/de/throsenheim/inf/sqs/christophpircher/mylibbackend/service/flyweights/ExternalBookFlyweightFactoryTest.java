package de.throsenheim.inf.sqs.christophpircher.mylibbackend.service.flyweights;

import de.throsenheim.inf.sqs.christophpircher.mylibbackend.api.OpenLibraryAPI;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExternalBookFlyweightFactoryTest {

    @Mock
    private OpenLibraryAPI openLibraryAPI;

    @InjectMocks
    private ExternalBookFlyweightFactory flyweightFactory;

    private static final String BOOK_ID = "OL1234567M";
    private static final Book DUMMY_BOOK = Book.builder()
            .bookID(BOOK_ID)
            .title("Title")
            .authors(List.of("Author"))
            .publishDate("2023")
            .build();

    private Field cacheField;

    @BeforeEach
    void setUp() throws Exception {
        cacheField = ExternalBookFlyweightFactory.class.getDeclaredField("bookCache");
        cacheField.setAccessible(true);
        getCache().clear();
    }

    @Test
    void getBookByIDShouldReturnCachedValueWhenNotExpired() throws Exception {
        CacheEntry<Optional<Book>> freshEntry = new CacheEntry<>(Optional.of(DUMMY_BOOK));

        getCache().put(BOOK_ID, freshEntry);

        Optional<Book> result = flyweightFactory.getBookByID(BOOK_ID);

        assertTrue(result.isPresent());
        assertEquals(DUMMY_BOOK, result.get());
        verify(openLibraryAPI, never()).getBookByBookID(anyString());
    }

    @Test
    void getBookByIDShouldFetchAndCacheWhenNotCached() throws Exception {
        when(openLibraryAPI.getBookByBookID(BOOK_ID)).thenReturn(Optional.of(DUMMY_BOOK));

        Optional<Book> result = flyweightFactory.getBookByID(BOOK_ID);

        assertTrue(result.isPresent());
        assertEquals(DUMMY_BOOK, result.get());
    }

    @Test
    void getBookByIDShouldFetchAndCacheWhenExpired() throws Exception {
        CacheEntry<Optional<Book>> expiredEntry = new CacheEntry<>(Optional.of(DUMMY_BOOK));

        // Force expiration by manipulating the timestamp
        Field timestampField = CacheEntry.class.getDeclaredField("timestamp");
        timestampField.setAccessible(true);
        timestampField.setLong(expiredEntry, System.currentTimeMillis() - (61L * 60 * 1000));

        getCache().put(BOOK_ID, expiredEntry);
        when(openLibraryAPI.getBookByBookID(BOOK_ID)).thenReturn(Optional.of(DUMMY_BOOK));

        Optional<Book> result = flyweightFactory.getBookByID(BOOK_ID);

        assertTrue(result.isPresent());
        assertEquals(DUMMY_BOOK, result.get());
    }

    @Test
    void getBookByIDShouldReturnEmptyOptionalAndCacheItWhenBookNotFound() throws Exception {
        when(openLibraryAPI.getBookByBookID(BOOK_ID)).thenReturn(Optional.empty());

        Optional<Book> result = flyweightFactory.getBookByID(BOOK_ID);

        assertTrue(result.isEmpty());
    }

    @Test
    void cleanupCacheShouldRemoveOnlyExpiredEntries() throws Exception {
        long now = System.currentTimeMillis();

        CacheEntry<Optional<Book>> validEntry = new CacheEntry<>(Optional.of(DUMMY_BOOK));
        CacheEntry<Optional<Book>> expiredEntry = new CacheEntry<>(Optional.of(DUMMY_BOOK));

        Field timestampField = CacheEntry.class.getDeclaredField("timestamp");
        timestampField.setAccessible(true);
        timestampField.setLong(expiredEntry, now - (61L * 60 * 1000));

        ConcurrentHashMap<String, CacheEntry<Optional<Book>>> map = getCache();
        map.put("valid", validEntry);
        map.put("expired", expiredEntry);

        Method cleanupMethod = ExternalBookFlyweightFactory.class.getDeclaredMethod("cleanupCache");
        cleanupMethod.setAccessible(true);
        cleanupMethod.invoke(flyweightFactory);

        assertEquals(1, map.size());
        assertTrue(map.containsKey("valid"));
    }

    @SuppressWarnings("unchecked")
    private ConcurrentHashMap<String, CacheEntry<Optional<Book>>> getCache() throws IllegalAccessException {
        return (ConcurrentHashMap<String, CacheEntry<Optional<Book>>>) cacheField.get(flyweightFactory);
    }
}
