package de.throsenheim.inf.sqs.christophpircher.mylibbackend.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/** Class for inner handling of search results */
@Data
@Builder
public class SearchResult {

    /** Number of total search results */
    private int numResults;

    /** Starting index within the total search results */
    private int startIndex;

    /** List of books contained in search result */
    private List<Book> searchResults;

}
