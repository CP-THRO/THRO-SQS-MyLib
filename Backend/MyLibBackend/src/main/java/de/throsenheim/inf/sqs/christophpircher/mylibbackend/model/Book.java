package de.throsenheim.inf.sqs.christophpircher.mylibbackend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Model Class for storing books in the database and internal handling of books.
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class Book {

    /**
     * Unique ID in the database
     */
    @Id
    private UUID id;

    /**
     * OpenLibrary API book id. Needed for duplicity check.
     */
    private String bookID;

    /**
     * Title of the book
     */
    private String title;

    /**
     * Subtitle of the book
     */
    private String subtitle;

    /**
     * List of authors of the book
     */
    private List<String> authors;

    /**
     * Description text for the book
     */
    private String description;

    /**
     * List of ISBNs. Contains ISBN-10 and ISBN-13 numbers
     */
    private List<String> isbns;

    /**
     * OpenLibrary cover image URL for the small sized version
     */
    private String coverURLSmall;

    /**
     * OpenLibrary cover image URL for the medium sized version
     */
    private String coverURLMedium;

    /**
     * OpenLibrary cover image URL for the large sized version
     */
    private String coverURLLarge;

    /**
     * Date/Year it was published
     */
    private String publishDate;

    /**
     * Mapping for the library many-to-many relationship with the users
     */
    @OneToMany(mappedBy = "book")
    private Set<LibraryBook> libraryBooks;

    /**
     * Mapping for the wishlist many-to-many relationship with the user
     */
    @ManyToMany
    private Set<User> wishlistUsers;

    /**
     * Get the average rating of all users
     * @return Average rating
     */
    public float getAverageRating() {
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
