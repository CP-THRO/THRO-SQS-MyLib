package de.throsenheim.inf.sqs.christophpircher.mylibbackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

/**
 * Class for storing users and their books in the database and for internal handling of users
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "usertable") //User is a SQL keyword...
public class User {
    /**
     * Unique Database key
     */
    @Id
    private UUID id;

    /**
     * Username of the user
     */
    private String username;

    /**
     * Bcrypt hash of the password
     */
    private String passwordHash;

    /**
     * Books in the library of the user
     */
    @OneToMany(mappedBy = "user")
    private Set<LibraryBook> libraryBooks;

    /**
     * Books on the wishlist of the library user
     */
    @ManyToMany
    private Set<Book> wishlistBooks;


}
