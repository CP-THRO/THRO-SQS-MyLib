package de.throsenheim.inf.sqs.christophpircher.mylibbackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.BookList;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * Data Transfer Object (DTO) for returning book search results to API callers.
 * <p>
 * Represents a paginated list of books returned from the OpenLibrary search endpoint,
 * along with total result count and start index for pagination purposes.
 * </p>
 *
 * <p>This class is typically returned by the {@code /search/external/keyword} endpoint.</p>
 *
 * @see BookDTO
 * @see BookList
 * @see de.throsenheim.inf.sqs.christophpircher.mylibbackend.controller.SearchController
 * @see de.throsenheim.inf.sqs.christophpircher.mylibbackend.service.SearchService
 *
 * Author:
 */
@Data
@Builder
public class BookListDTO {
    /**
     * Total number of search results available (ignores pagination).
     */
    @JsonProperty("numResults")
    @Schema(description = "Number of total results (without pagination)", example = "801")
    private int numResults;

    /**
     * Index of the first result in the current page (zero-based).
     */
    @JsonProperty("startIndex")
    @Schema(description = "Start index within the total number of search results", example = "100")
    private int startIndex;

    /**
     * List of books returned for the current page.
     */
    @JsonProperty("books")
    @Schema(description = "Book DTOs of search result or database or library")
    private List<BookDTO> books;

    /**
     * Due to some inconsistencies in the OpenLibrary, some search results will be skipped if they don't have an associated addition.
     */
    @JsonProperty("skippedBooks")
    @Schema(description = "Due to some inconsistencies in the OpenLibrary, some search results will be skipped if they don't have an associated addition", example = "1")
    @Builder.Default
    private int skippedBooks = 0;

    /**
     * Converts a {@link BookList} domain model into a {@link BookListDTO} for API response.
     *
     * @param bookList the domain model to convert
     * @return a {@link BookListDTO} populated with converted data
     */
    public static BookListDTO fromSearchResult(BookList bookList) {
        BookListDTOBuilder builder = BookListDTO.builder();
        builder.numResults(bookList.getNumResults());
        builder.startIndex(bookList.getStartIndex());
        builder.skippedBooks(bookList.getSkippedBooks());
        List<BookDTO> bookDTOS = bookList.getBooks().stream().map(BookDTO::fromBook).toList();

        builder.books(bookDTOS);
        return builder.build();
    }
}
