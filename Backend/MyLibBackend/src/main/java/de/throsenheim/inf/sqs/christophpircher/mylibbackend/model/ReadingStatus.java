package de.throsenheim.inf.sqs.christophpircher.mylibbackend.model;

/**
 * Enumeration representing the reading status of a book for a specific user.
 * <p>
 * Used in the {@link LibraryBook} entity to indicate whether the user has read,
 * is currently reading, or has not yet read the book.
 * </p>
 *
 * @see LibraryBook
 */
public enum ReadingStatus {
    /**
     * The book has not been read yet by the user.
     */
    UNREAD,
    /**
     * The user is currently reading the book.
     */
    READING,
    /**
     * The user has finished reading the book.
     */
    READ
}
