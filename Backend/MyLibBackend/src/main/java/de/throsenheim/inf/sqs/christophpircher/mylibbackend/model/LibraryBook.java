package de.throsenheim.inf.sqs.christophpircher.mylibbackend.model;

import jakarta.persistence.*;
import lombok.Getter;

/**
 * Helper class for the many-to-many relationship between the books and the users. Necessary because of the extra fields for rating and reading status.
 * Adapted from <a href="https://www.baeldung.com/jpa-many-to-many">https://www.baeldung.com/jpa-many-to-many</a>
 */
@Entity
@Getter
public class LibraryBook {

    /**
     * Foreign/Primary keys for the relationship
     */
    @EmbeddedId
    private LibraryBookKey id;

    /**
     * Mapping to the book
     */
    @ManyToOne
    @MapsId("bookId")
    @JoinColumn(name="book_id")
    private Book book;

    /**
     * Mapping to the user
     */
    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name="user_id")
    private User user;

    /**
     * Relationship-specific attribute: User rating between 1 and 5 for the book
     */
    private int rating;

    /**
     * Relationship-specific attribute: User reading status.
     */
    private ReadingStatus readingStatus;
}
