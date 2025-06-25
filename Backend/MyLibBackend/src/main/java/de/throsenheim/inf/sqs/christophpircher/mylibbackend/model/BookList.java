package de.throsenheim.inf.sqs.christophpircher.mylibbackend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Internal model class for handling search results returned by the OpenLibrary API or internal book repository.
 * <p>
 * Used to encapsulate the pagination metadata and the list of matching books.
 * </p>
 *
 * @see Book
 */
@Data
@Builder
@AllArgsConstructor //for unit test
@NoArgsConstructor
public class BookList {

    /**
     * The total number of search results found for the given query.
     */
    private int numResults;

    /**
     * The index of the first search result returned (used for pagination).
     */
    private int startIndex;

    /**
     * The list of {@link Book} objects included in this page of search results or database or library.
     */
    private List<Book> books;

    /**
     * Due to some inconsistencies in the OpenLibrary, some search results will be skipped if they don't have an associated addition.
     */
    @Builder.Default
    private int skippedBooks = 0;

}
