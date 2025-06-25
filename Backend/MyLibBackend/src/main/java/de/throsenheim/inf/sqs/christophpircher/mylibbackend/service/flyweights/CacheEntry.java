package de.throsenheim.inf.sqs.christophpircher.mylibbackend.service.flyweights;

import java.time.Clock;

/**
 * A simple wrapper for cached values that includes a timestamp
 * to support time-based expiration logic (TTL - Time To Live).
 *
 * @param <T> The type of object being cached.
 */
class CacheEntry<T> {

    /**
     * The actual cached value.
     */
    final T value;

    /**
     * The timestamp when the entry was created (in milliseconds).
     */
    final long timestamp;

    /**
     * Clock for generating timestamp. Using clock because it is mockable
     */
    private final Clock clock;

    /**
     * Creates a new CacheEntry and records the current timestamp.
     *
     * @param value The object to cache.
     */
    CacheEntry(T value) {
        this.value = value;
        this.timestamp = System.currentTimeMillis();
        this.clock = Clock.systemDefaultZone();
    }

    /**
     * Constructor for testing (injectable Clock)
      */
    CacheEntry(T value, Clock clock) {
        this.value = value;
        this.clock = clock;
        this.timestamp = clock.millis();
    }

    /**
     * Determines whether this cache entry is still valid based on TTL.
     *
     * @param ttlMillis Time-to-live in milliseconds.
     * @return true if not expired, false otherwise.
     */
    boolean isNotExpired(long ttlMillis) {
        return clock.millis() - timestamp <= ttlMillis;
    }

    /**
     * Determines whether this entry is expired at a specific point in time.
     * Useful for batch cleanup using a shared timestamp.
     *
     * @param now       The current time in milliseconds.
     * @param ttlMillis Time-to-live in milliseconds.
     * @return true if expired, false otherwise.
     */
    boolean isExpired(long now, long ttlMillis) {
        return now - timestamp > ttlMillis;
    }
}