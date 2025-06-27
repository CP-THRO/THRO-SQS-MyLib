package de.throsenheim.inf.sqs.christophpircher.mylibbackend.model;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BookTest {

    @Test
    void testGetAverageRatingWithNoLibraryBooks() {
        Book book = Book.builder().libraryBooks(null).build();

        assertEquals(0.0f, book.getAverageRating());
        assertEquals(0.0f, book.getAverageRating());
    }

    @Test
    void testGetAverageRatingWithValidRatings() {
        LibraryBook lb1 = new LibraryBook();
        lb1.setRating(4);

        LibraryBook lb2 = new LibraryBook();
        lb2.setRating(5);

        Book book = Book.builder().libraryBooks(Set.of(lb1, lb2)).build();

        assertEquals(4.5f, book.getAverageRating(), 0.01f);
    }

    @Test
    void testGetAverageRatingSkipsZeroRatings() {
        LibraryBook lb1 = new LibraryBook();
        lb1.setRating(0);

        LibraryBook lb2 = new LibraryBook();
        lb2.setRating(3);

        Book book = Book.builder().libraryBooks(Set.of(lb1, lb2)).build();

        assertEquals(3.0f, book.getAverageRating(), 0.01f);
    }

    @Test
    void testGetAverageRatingAllZeroRatings() {

        LibraryBookKey keyb1 = new LibraryBookKey(UUID.randomUUID(), UUID.randomUUID());
        LibraryBookKey keyb2 = new LibraryBookKey(UUID.randomUUID(), UUID.randomUUID());

        LibraryBook lb1 = new LibraryBook();
        lb1.setId(keyb1);
        lb1.setRating(0);

        LibraryBook lb2 = new LibraryBook();
        lb2.setRating(0);
        lb2.setId(keyb2);

        Book book = Book.builder().libraryBooks(Set.of(lb1, lb2)).build();

        assertEquals(0.0f, book.getAverageRating(), 0.01f);
    }
}