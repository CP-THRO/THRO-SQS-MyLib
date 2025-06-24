package de.throsenheim.inf.sqs.christophpircher.mylibbackend.service.flyweights;

import de.throsenheim.inf.sqs.christophpircher.mylibbackend.api.OpenLibraryAPI;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.exceptions.UnexpectedStatusException;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.Book;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Caches book details from the OpenLibrary API to avoid redundant network calls.
 *
 * Uses a thread-safe ConcurrentHashMap to store flyweight entries (shared, immutable objects),
 * each wrapped in a CacheEntry with a creation timestamp.
 *
 * Entries are valid for a configurable TTL (default: 60 minutes),
 * after which they are considered expired and reloaded on next access.
 */
@AllArgsConstructor
@Component // Makes this a singleton Spring-managed bean
public class ExternalBookFlyweightFactory {

    // Cache mapping bookID -> Optional<Book> wrapped in a timestamped entry
    private final ConcurrentHashMap<String, CacheEntry<Optional<Book>>> bookCache = new ConcurrentHashMap<>();

    // Time-to-live for cached entries (60 minutes)
    private static final long TTL_MILLIS = 60L * 60 * 1000;

    private final OpenLibraryAPI openLibraryAPI;

    /**
     * Fetches a book by its OpenLibrary ID, either from cache or fresh via API.
     *
     * If the book is cached and not expired, it returns the cached value.
     * Otherwise, it fetches the data from the API and stores it in the cache.
     *
     * @param bookID OpenLibrary book identifier (e.g., "OL1234567M")
     * @return an Optional<Book>, empty if the book doesn't exist
     * @throws UnexpectedStatusException if the API returns an unexpected status code
     * @throws IOException on network or parsing failures
     */
    public Optional<Book> getBookByID(String bookID) throws UnexpectedStatusException, IOException {
        CacheEntry<Optional<Book>> cached = bookCache.get(bookID);

        if (cached != null && cached.isNotExpired(TTL_MILLIS)) {
            return cached.value;
        }

        Optional<Book> book = openLibraryAPI.getBookByBookID(bookID);
        bookCache.put(bookID, new CacheEntry<>(book));
        return book;
    }

    /**
     * Periodically clears expired cache entries based on TTL.
     *
     * This is scheduled to run every 10 minutes and prevents unbounded memory usage.
     */
    @Scheduled(fixedDelay = 10 * 60 * 1000) // Every 10 minutes
    public void cleanupCache() {
        long now = System.currentTimeMillis();
        bookCache.entrySet().removeIf(entry -> entry.getValue().isExpired(now, TTL_MILLIS));
    }

}