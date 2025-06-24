package de.throsenheim.inf.sqs.christophpircher.mylibbackend.service;

import de.throsenheim.inf.sqs.christophpircher.mylibbackend.api.OpenLibraryAPI;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.exceptions.UnexpectedStatusException;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.Book;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.BookList;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

/**
 * Service class for handling search-related functionality using the OpenLibrary API.
 * <p>
 * Acts as a thin wrapper around {@link OpenLibraryAPI} to decouple controller logic from API access logic.
 * </p>
 */
@Service
@Slf4j
@AllArgsConstructor
public class SearchService {

    private OpenLibraryAPI openLibraryAPI;

    /**
     * Searches for books using the OpenLibrary API with the provided keyword string.
     * Used primarily for full-text search features.
     *
     * @param keywords        the keywords to search for
     * @param startIndex      the starting index for pagination
     * @param numResultsToGet the number of results to retrieve
     * @return {@link BookList} object containing a list of books and metadata
     * @throws UnexpectedStatusException if the OpenLibrary API returns an unexpected status code
     * @throws IOException               if a connection or read error occurs
     */
    public BookList searchKeywordsExternal(String keywords, int startIndex, int numResultsToGet) throws UnexpectedStatusException, IOException {
        return openLibraryAPI.searchBooks(keywords, startIndex, numResultsToGet);
    }

}
