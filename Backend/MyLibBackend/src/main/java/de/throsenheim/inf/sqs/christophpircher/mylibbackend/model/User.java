package de.throsenheim.inf.sqs.christophpircher.mylibbackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Entity class representing a user of the application.
 * <p>
 * This class stores authentication details as well as the user's personal library and wishlist.
 * </p>
 *
 * @see Book
 * @see LibraryBook
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Table(name = "usertable") //User is a SQL keyword...
public class User {
    /**
     * Unique identifier for the user in the database.
     */
    @Id
    private UUID id;

    /**
     * Username of the user. Must be unique within the application.
     */
    private String username;

    /**
     * Password hash (bcrypt) used for authentication.
     */
    private String passwordHash;

    /**
     * The books owned by the user, including metadata such as rating and reading status.
     * <p>
     * This is part of a many-to-many relationship using a join entity {@link LibraryBook}.
     * </p>
     */
    @OneToMany(mappedBy = "user")
    private Set<LibraryBook> libraryBooks;

    /**
     * The books the user has added to their wishlist.
     * <p>
     * This is a many-to-many relationship directly between users and books.
     * </p>
     */
    @ManyToMany
    @Builder.Default
    private Set<Book> wishlistBooks = new HashSet<>();


}
