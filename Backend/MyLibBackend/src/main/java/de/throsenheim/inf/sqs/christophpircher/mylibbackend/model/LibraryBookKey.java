package de.throsenheim.inf.sqs.christophpircher.mylibbackend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.UUID;



/**
 * Embeddable class representing the composite primary key for the {@link LibraryBook} entity.
 * <p>
 * This key is composed of the foreign keys from both the {@link Book} and {@link User} entities.
 * It is used in the many-to-many relationship between users and books, allowing for extra attributes
 * (such as ratings and reading status) to be added on the relationship.
 * </p>
 *
 * @see LibraryBook
 * @see Book
 * @see User
 */
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class LibraryBookKey {
    /**
     * UUID of the associated book.
     * <p>This acts as a foreign key to the {@link Book} entity and is also part of the composite primary key.</p>
     */
    @Column(name="book_id")
    private UUID bookId;

    /**
     * UUID of the associated user.
     * <p>This acts as a foreign key to the {@link User} entity and is also part of the composite primary key.</p>
     */
    @Column(name="user_id")
    private UUID userId;
}
