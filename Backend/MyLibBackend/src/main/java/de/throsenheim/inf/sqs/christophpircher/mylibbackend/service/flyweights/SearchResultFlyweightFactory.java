package de.throsenheim.inf.sqs.christophpircher.mylibbackend.service.flyweights;

import de.throsenheim.inf.sqs.christophpircher.mylibbackend.api.OpenLibraryAPI;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.exceptions.UnexpectedStatusException;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.BookList;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Caches paginated keyword-based search results from the OpenLibrary API to improve performance.
 *
 * Uses a ConcurrentHashMap to ensure thread-safe caching.
 * Each entry is wrapped in a {@link CacheEntry} to support time-based expiration (TTL).
 *
 * The cache helps avoid redundant API calls for repeated or paginated searches,
 * while keeping memory usage in check via scheduled cleanup.
 */
@AllArgsConstructor
@Component // Makes this class a singleton in the Spring context
public class SearchResultFlyweightFactory {

    // Thread-safe cache of keyword-based search results
    private final ConcurrentHashMap<SearchResultFlyweightKey, CacheEntry<BookList>> bookListCache = new ConcurrentHashMap<>();

    private final OpenLibraryAPI openLibraryAPI;

    // Time-to-live for cache entries (60 minutes)
    private static final long TTL_MILLIS = 60L * 60 * 1000;

    /**
     * Retrieves search results from cache or fetches them from the OpenLibrary API.
     *
     * If the same keyword + pagination combination is already cached and still valid, it is reused.
     * Otherwise, a fresh API call is made and cached for future use.
     *
     * @param keywords   Search keywords
     * @param startIndex Pagination start index
     * @param numToGet   Number of books to retrieve
     * @return A {@link BookList} containing the search results
     * @throws UnexpectedStatusException if the OpenLibrary API returns a bad response
     * @throws IOException on network failure
     */
    public BookList search(String keywords, int startIndex, int numToGet) throws UnexpectedStatusException, IOException {
        SearchResultFlyweightKey key = new SearchResultFlyweightKey(keywords, startIndex, numToGet);
        CacheEntry<BookList> cachedEntry = bookListCache.get(key);

        // Sonar prefers computeIfAbsent, but it's not suitable here because we need exception handling and TTL checks
        if (cachedEntry != null && cachedEntry.isNotExpired(TTL_MILLIS)) {
            return cachedEntry.value;
        }

        // Load from API and update cache
        BookList bookList = openLibraryAPI.searchBooks(keywords, startIndex, numToGet);
        bookListCache.put(key, new CacheEntry<>(bookList));
        return bookList;
    }

    /**
     * Periodically cleans up expired entries from the cache to prevent memory leaks.
     * Runs every 10 minutes.
     */
    @Scheduled(fixedDelay = 10 * 60 * 1000) // Every 10 minutes
    public void cleanupCache() {
        long now = System.currentTimeMillis();
        bookListCache.entrySet().removeIf(entry -> entry.getValue().isExpired(now, TTL_MILLIS));
    }

    /**
     * Composite key representing a unique search query, based on keywords, start index, and result count.
     * Used for deduplication and lookup in the cache.
     */
    private record SearchResultFlyweightKey(String keywords, int startIndex, int numToGet) { }
}
