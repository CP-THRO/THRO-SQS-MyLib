package de.throsenheim.inf.sqs.christophpircher.mylibbackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Entity representing a book in the system.
 * <p>
 * This model is used both for internal application logic and for persistent storage in the database.
 * It includes data obtained from the OpenLibrary API and user-specific metadata such as library ownership and ratings.
 * </p>
 *
 * @see LibraryBook
 * @see User
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class Book {

    /**
     * Unique identifier for this book record in the database.
     */
    @Id
    private UUID id;

    /**
     * OpenLibrary book identifier (e.g., "OL9698350M").
     * Used to prevent duplicate imports and link with OpenLibrary data.
     */
    private String bookID;

    /**
     * Title of the book.
     */
    private String title;

    /**
     * Subtitle of the book, if available.
     */
    private String subtitle;

    /**
     * List of authors.
     */
    private List<String> authors;

    /**
     * Description or summary text for the book.
     */
    private String description;

    /**
     * List of ISBNs, including both ISBN-10 and ISBN-13 values.
     */
    private List<String> isbns;

    /**
     * URL to a small-sized cover image provided by OpenLibrary.
     */
    private String coverURLSmall;

    /**
     * URL to a medium-sized cover image provided by OpenLibrary.
     */
    private String coverURLMedium;

    /**
     * URL to a large-sized cover image provided by OpenLibrary.
     */
    private String coverURLLarge;

    /**
     * Publish date of the book, as provided by OpenLibrary.
     */
    private String publishDate;

    /**
     * Mapping to the {@link LibraryBook} entities, which represent a many-to-many relationship
     * between users and books stored in their libraries, with additional metadata like rating.
     */
    @OneToMany(mappedBy = "book")
    private Set<LibraryBook> libraryBooks;

    /**
     * Many-to-many mapping between books and users who have added this book to their wishlist.
     */
    @ManyToMany
    private Set<User> wishlistUsers;

    /**
     * Computes the average rating of this book across all users who have rated it.
     *
     * @return The average rating (between 1 and 5), or 0 if there are no ratings.
     */
    public float getAverageRating() {
        if(libraryBooks == null || libraryBooks.isEmpty()) {
            return 0;
        }
        float sum = 0;
        int count = 0;
        for(LibraryBook libraryBook : libraryBooks){
            if(libraryBook.getRating() > 0){
                sum += libraryBook.getRating();
                ++count;
            }
        }
        if(count != 0){
            return  sum/count;
        }else{
            return sum;
        }
    }
}
