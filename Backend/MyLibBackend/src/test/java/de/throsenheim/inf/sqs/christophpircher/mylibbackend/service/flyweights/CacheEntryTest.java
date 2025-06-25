package de.throsenheim.inf.sqs.christophpircher.mylibbackend.service.flyweights;

import org.junit.jupiter.api.Test;

import java.time.Clock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CacheEntryTest {

    private static final long TTL = 1000L; // 1 second TTL

    @Test
    void isNotExpiredShouldReturnTrueWhenEntryIsFresh() {
        CacheEntry<String> entry = new CacheEntry<>("test");
        assertTrue(entry.isNotExpired(TTL));
    }

    @Test
    void isNotExpiredShouldReturnFalseWhenEntryIsExpiredUsingMockClock() {
        Clock mockClock = mock(Clock.class);

        long baseTime = 1_000_000L;
        when(mockClock.millis()).thenReturn(baseTime);

        CacheEntry<String> entry = new CacheEntry<>("stale", mockClock);

        // Advance time beyond TTL
        when(mockClock.millis()).thenReturn(baseTime + TTL + 1);

        assertFalse(entry.isNotExpired(TTL));
    }

    @Test
    void isExpiredShouldReturnFalseWhenNotExpiredAtGivenTime() {
        CacheEntry<String> entry = new CacheEntry<>("test");
        long now = entry.timestamp + TTL - 1;
        assertFalse(entry.isExpired(now, TTL));
    }

    @Test
    void isExpiredShouldReturnTrueWhenExpiredAtGivenTime() {
        CacheEntry<String> entry = new CacheEntry<>("test");
        long now = entry.timestamp + TTL + 1;
        assertTrue(entry.isExpired(now, TTL));
    }

    @Test
    void valueShouldBeStoredCorrectly() {
        String value = "myData";
        CacheEntry<String> entry = new CacheEntry<>(value);
        assertEquals(value, entry.value);
    }

    @Test
    void timestampShouldBeLessThanOrEqualToNow() {
        CacheEntry<String> entry = new CacheEntry<>("checkTime");
        long now = System.currentTimeMillis();
        assertTrue(entry.timestamp <= now);
    }
}