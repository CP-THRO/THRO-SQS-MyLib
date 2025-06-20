package de.throsenheim.inf.sqs.christophpircher.mylibbackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.Book;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Data Transfer Object (DTO) for representing book details in API responses.
 * <p>
 * This class is used to send book information to frontend clients via the REST API.
 * It encapsulates all relevant data such as title, authors, ISBNs, cover image URLs,
 * publication date, description, and rating metadata.
 * </p>
 *
 * <p>It also includes a static {@link #fromBook(Book)} method for easy conversion from the domain model.</p>
 *
 * @see Book
 * @see de.throsenheim.inf.sqs.christophpircher.mylibbackend.controller.SearchController
 * @see de.throsenheim.inf.sqs.christophpircher.mylibbackend.service.BookService
 * @see Schema
 *
 */
@Data
@Builder
public class BookDTO {

    /**
     * OpenLibrary Book ID. Used as a unique identifier in OpenLibrary and the local database.
     */
    @JsonProperty("bookID")
    @Schema(description = "OpenLibrary Book ID. Used to identify books from the OpenLibrary API and internally stored books.", example = "OL9698350M")
    private String bookID;

    /**
     * Title of the book.
     */
    @JsonProperty("title")
    @Schema(description = "Book title", example = "Mass effect")
    private String title;

    /**
     * Subtitle of the book, if available.
     */
    @JsonProperty("subtitle")
    @Schema(description = "Book Subtitle. Not included in search result.", example = "Ascension")
    private String subtitle;

    /**
     * List of all authors.
     */
    @JsonProperty("authors")
    @Schema(description = "List with all authors of the book", example = "[\"Drew Karpyshyn\"]")
    private List<String> authors;

    /**
     * Long-form description of the book.
     */
    @JsonProperty("description")
    @Schema(description = "Description text of the book. Not included in search result", example = "Every advanced society in the galaxy relies on the technology of the Protheans, an ancient species that vanished fifty thousand years ago. After discovering a cache of Prothean technology on Mars in 2148, humanity is spreading to the stars; the newest interstellar species, struggling to carve out its place in the greater galactic community.On the edge of colonized space, ship commander and Alliance war hero David Anderson investigates the remains of a top secret military research station; smoking ruins littered with bodies and unanswered questions. Who attacked this post and for what purpose? And where is Kahlee Sanders, the young scientist who mysteriously vanished from the base--hours before her colleagues were slaughtered?Sanders is now the prime suspect, but finding her creates more problems for Anderson than it solves. Partnered with a rogue alien agent he can't trust and pursued by an assassin he can't escape, Anderson battles impossible odds on uncharted worlds to uncover a sinister conspiracy . . . one he won't live to tell about. Or so the enemy thinks.From the Paperback edition.")
    private String description;

    /**
     * List of ISBN identifiers (ISBN-10 and/or ISBN-13).
     */
    @JsonProperty("isbns")
    @Schema(description = "If found in the API, this contains the ISBN numbers of the book. This contains both ISBN-10 and ISBN-13 numbers. Not included in search result", example = "[\"9780345498526\"]")
    private List<String> isbns;

    /**
     * URL to the small cover image.
     */
    @JsonProperty("coverURLSmall")
    @Schema(description = "URL to the small cover image of the book", example = "https://covers.openlibrary.org/b/id/12394458-S.jpg")
    private String coverURLSmall;

    /**
     * URL to the medium cover image.
     */
    @JsonProperty("coverURLMedium")
    @Schema(description = "URL to the medium cover image of the book", example = "https://covers.openlibrary.org/b/id/12394458-M.jpg")
    private String coverURLMedium;

    /**
     * URL to the large cover image.
     */
    @JsonProperty("coverURLLarge")
    @Schema(description = "URL to the large cover image of the book", example = "https://covers.openlibrary.org/b/id/12394458-L.jpg")
    private String coverURLLarge;

    /**
     * Year or date the book was (first) published.
     */
    @JsonProperty("publishDate")
    @Schema(description = "Year or date the book was (first) published", example = "May 1, 2007")
    private String publishDate;

    /**
     * User's individual rating for the book, from 1 to 5. 0 = no rating yet.
     */
    @JsonProperty("individualRating")
    @Schema(description = "Rating of a book from 1 to 5 of the user. 0 = no rating yet", example = "4")
    @Builder.Default
    private int individualRating = 0;

    /**
     * Average rating across all users, from 1 to 5. 0 = no ratings yet.
     */
    @JsonProperty("averageRating")
    @Schema(description = "Average rating of a book from 1 to 5 of all users that have rated the book. 0 = no rating yet", example = "4.5")
    @Builder.Default
    private float averageRating = 0;

    /**
     * Converts a {@link Book} domain model into a {@link BookDTO} for API output.
     *
     * @param book The {@link Book} object to convert
     * @return A {@link BookDTO} with values mapped from the {@code book}
     */
    public static BookDTO fromBook(Book book) {
        BookDTO.BookDTOBuilder builder = BookDTO.builder();
        builder.bookID(book.getBookID());
        builder.title(book.getTitle());
        builder.subtitle(book.getSubtitle());
        builder.authors(book.getAuthors());
        builder.description(book.getDescription());
        builder.isbns(book.getIsbns());
        builder.publishDate(book.getPublishDate());
        builder.coverURLSmall(book.getCoverURLSmall());
        builder.coverURLMedium(book.getCoverURLMedium());
        builder.coverURLLarge(book.getCoverURLLarge());
        builder.averageRating(book.getAverageRating());
        return builder.build();
    }
}
