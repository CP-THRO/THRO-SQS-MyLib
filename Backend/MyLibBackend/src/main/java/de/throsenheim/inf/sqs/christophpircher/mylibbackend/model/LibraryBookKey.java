package de.throsenheim.inf.sqs.christophpircher.mylibbackend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.UUID;


/**
 * Class for the mixed primary/foreign keys of the library many-to-many relationship of the user.
 */
@Embeddable
public class LibraryBookKey {
    @Column(name="book_id")
    private UUID bookId;
    @Column(name="user_id")
    private UUID userId;
}
