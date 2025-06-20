package de.throsenheim.inf.sqs.christophpircher.mylibbackend.model;

import lombok.Builder;
import lombok.Data;

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
public class SearchResult {

    /**
     * The total number of search results found for the given query.
     */
    private int numResults;

    /**
     * The index of the first search result returned (used for pagination).
     */
    private int startIndex;

    /**
     * The list of {@link Book} objects included in this page of search results.
     */
    private List<Book> searchResults;

}
