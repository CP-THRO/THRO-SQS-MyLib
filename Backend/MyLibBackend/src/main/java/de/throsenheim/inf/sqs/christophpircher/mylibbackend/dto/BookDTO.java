package de.throsenheim.inf.sqs.christophpircher.mylibbackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.Book;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * DTO for returning books to the API caller
 */
@Data
@Builder
public class BookDTO {

    /**
     * OpenLibrary API book id. Needed for duplicity check.
     */
    @JsonProperty("bookID")
    @Schema(description = "OpenLibrary Book ID. Used to identify books from the OpenLibrary API and internally stored books.", example = "OL9698350M")
    private String bookID;

    /**
     * Title of the book
     */
    @JsonProperty("title")
    @Schema(description = "Book title", example = "Mass effect")
    private String title;

    /**
     * Subtitle of the book
     */
    @JsonProperty("subtitle")
    @Schema(description = "Book Subtitle", example = "Ascension")
    private String subtitle;

    /**
     * List of authors of the book
     */
    @JsonProperty("authors")
    @Schema(description = "List with all authors of the book", example = "[\"Drew Karpyshyn\"]")
    private List<String> authors;

    /**
     * Description text for the book
     */
    @JsonProperty("description")
    @Schema(description = "Description text of the book", example = "Every advanced society in the galaxy relies on the technology of the Protheans, an ancient species that vanished fifty thousand years ago. After discovering a cache of Prothean technology on Mars in 2148, humanity is spreading to the stars; the newest interstellar species, struggling to carve out its place in the greater galactic community.On the edge of colonized space, ship commander and Alliance war hero David Anderson investigates the remains of a top secret military research station; smoking ruins littered with bodies and unanswered questions. Who attacked this post and for what purpose? And where is Kahlee Sanders, the young scientist who mysteriously vanished from the base--hours before her colleagues were slaughtered?Sanders is now the prime suspect, but finding her creates more problems for Anderson than it solves. Partnered with a rogue alien agent he can't trust and pursued by an assassin he can't escape, Anderson battles impossible odds on uncharted worlds to uncover a sinister conspiracy . . . one he won't live to tell about. Or so the enemy thinks.From the Paperback edition.")
    private String description;

    /**
     * List of ISBNs. Contains ISBN-10 and ISBN-13 numbers
     */
    @JsonProperty("isbns")
    @Schema(description = "If found in the API, this contains the ISBN numbers of the book. This contains both ISBN-10 and ISBN-13 numbers", example = "[\"9780345498526\"]")
    private List<String> isbns;

    /**
     * OpenLibrary cover image URL for the small sized version
     */
    @JsonProperty("coverURLSmall")
    @Schema(description = "URL to the small cover image of the book", example = "https://covers.openlibrary.org/b/id/12394458-S.jpg")
    private String coverURLSmall;

    /**
     * OpenLibrary cover image URL for the medium sized version
     */
    @JsonProperty("coverURLMedium")
    @Schema(description = "URL to the medium cover image of the book", example = "https://covers.openlibrary.org/b/id/12394458-M.jpg")
    private String coverURLMedium;

    /**
     * OpenLibrary cover image URL for the large sized version
     */
    @JsonProperty("coverURLLarge")
    @Schema(description = "URL to the large cover image of the book", example = "https://covers.openlibrary.org/b/id/12394458-L.jpg")
    private String coverURLLarge;

    /**
     * Date/Year it was published
     */
    @JsonProperty("publishDate")
    @Schema(description = "Year or date the book was (first) published", example = "May 1, 2007")
    private String publishDate;

    /**
     * Rating between 1 and 5. Average for all books in library page, Individual for personal library page.
     */
    @JsonProperty("rating")
    @Schema(description = "Rating of a book from 1 to 5. Not existent, Average or user-specific, depending on the context", example = "4")
    @Builder.Default
    private int rating = 0;

    /**
     * Generate a BookDTO from a Book
     * @param book Book to convert
     * @return BookDTO to send to the API caller
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
        return builder.build();
    }
}
