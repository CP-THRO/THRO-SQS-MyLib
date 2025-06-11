package de.throsenheim.inf.sqs.christophpircher.mylibbackend.service;

import de.throsenheim.inf.sqs.christophpircher.mylibbackend.api.OpenLibraryAPI;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.exceptions.UnexpectedStatusException;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.SearchResult;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Handles the internal search logic
 */
@Service
@Slf4j
@AllArgsConstructor
public class SearchService {

    private OpenLibraryAPI openLibraryAPI;

    /**
     * Calls the OpenLibraryAPI to get the search results for the keywords
     * @param keywords Keywords to search for
     * @param startIndex Starting index from which to get the results (for pagination)
     * @param numResultsToGet Number of results to get (for pagination)
     * @return Search result object
     * @throws UnexpectedStatusException If the API returns an unexpected status
     * @throws IOException If something goes wrong with the connection
     */
    public SearchResult searchKeywordsExternal(String keywords, int startIndex, int numResultsToGet) throws UnexpectedStatusException, IOException {
        return openLibraryAPI.searchBooks(keywords, startIndex, numResultsToGet);
    }

}
