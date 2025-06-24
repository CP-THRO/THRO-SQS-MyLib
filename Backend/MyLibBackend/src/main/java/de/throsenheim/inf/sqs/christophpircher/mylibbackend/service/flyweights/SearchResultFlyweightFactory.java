package de.throsenheim.inf.sqs.christophpircher.mylibbackend.service.flyweights;

import de.throsenheim.inf.sqs.christophpircher.mylibbackend.api.OpenLibraryAPI;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.exceptions.UnexpectedStatusException;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.BookList;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Caches paginated keyword-based search results from the OpenLibrary API to improve performance.
 * Uses a ConcurrentHashMap to ensure thread-safe caching.
 * Each entry is wrapped in a {@link CacheEntry} to support time-based expiration (TTL).
 * The cache helps avoid redundant API calls for repeated or paginated searches,
 * while keeping memory usage in check via scheduled cleanup.
 */
@Slf4j
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

        if (cachedEntry != null && cachedEntry.isNotExpired(TTL_MILLIS)) {
            log.debug("Cache hit for search [keywords='{}', start={}, count={}]", keywords, startIndex, numToGet);
            return cachedEntry.value;
        }

        if (cachedEntry != null) {
            log.debug("Cache expired for search [keywords='{}', start={}, count={}], fetching new data", keywords, startIndex, numToGet);
        } else {
            log.debug("Cache miss for search [keywords='{}', start={}, count={}], fetching from OpenLibrary", keywords, startIndex, numToGet);
        }

        BookList bookList = openLibraryAPI.searchBooks(keywords, startIndex, numToGet);
        bookListCache.put(key, new CacheEntry<>(bookList));

        log.info("Search result fetched and cached: [keywords='{}'] - {} books returned", keywords, bookList.getBooks().size());

        return bookList;
    }

    /**
     * Periodically cleans up expired entries from the cache to prevent memory leaks.
     * Runs every 10 minutes.
     */
    @Scheduled(fixedDelay = 10 * 60 * 1000) // Every 10 minutes
    private void cleanupCache() {
        long now = System.currentTimeMillis();
        int before = bookListCache.size();
        bookListCache.entrySet().removeIf(entry -> entry.getValue().isExpired(now, TTL_MILLIS));
        int after = bookListCache.size();
        int removed = before - after;

        if (removed > 0) {
            log.info("Search cache cleanup: {} expired entries removed ({} remaining)", removed, after);
        } else {
            log.debug("Search cache cleanup: no expired entries ({} total)", after);
        }
    }


    /**
     * Composite key representing a unique search query, based on keywords, start index, and result count.
     * Used for deduplication and lookup in the cache.
     */
    private record SearchResultFlyweightKey(String keywords, int startIndex, int numToGet) { }
}
