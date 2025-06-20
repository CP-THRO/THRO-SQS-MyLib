package de.throsenheim.inf.sqs.christophpircher.mylibbackend.model;

import jakarta.persistence.*;
import lombok.Getter;

/**
 * Entity class representing the many-to-many relationship between {@link User} and {@link Book} entities.
 * <p>
 * This intermediate entity allows for additional fields on the relationship, such as user-specific
 * rating and reading status. This pattern is often referred to as an "association entity."
 * </p>
 *
 * <p>Adapted from:
 * <a href="https://www.baeldung.com/jpa-many-to-many">Baeldung JPA Many-to-Many Guide</a></p>
 *
 * @see LibraryBookKey
 * @see ReadingStatus
 * @see User
 * @see Book
 */
@Entity
@Getter
public class LibraryBook {

    /**
     * Composite key containing {@code userId} and {@code bookId} for uniquely identifying the relationship.
     */
    @EmbeddedId
    private LibraryBookKey id;

    /**
     * Reference to the associated {@link Book}.
     * <p>Uses {@code @MapsId("bookId")} to link to the composite key.</p>
     */
    @ManyToOne
    @MapsId("bookId")
    @JoinColumn(name="book_id")
    private Book book;

    /**
     * Reference to the associated {@link User}.
     * <p>Uses {@code @MapsId("userId")} to link to the composite key.</p>
     */
    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name="user_id")
    private User user;

    /**
     * User-specific rating for the book.
     * <p>
     * Should be in the range 1 to 5. A value of 0 indicates no rating.
     * </p>
     */
    private int rating;

    /**
     * Reading status of the book for the associated user.
     * <p>
     * This is an enum field representing whether the user has read, is reading, or plans to read the book.
     * </p>
     */
    private ReadingStatus readingStatus;
}
