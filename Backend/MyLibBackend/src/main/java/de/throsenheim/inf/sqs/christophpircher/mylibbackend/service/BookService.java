package de.throsenheim.inf.sqs.christophpircher.mylibbackend.service;

import de.throsenheim.inf.sqs.christophpircher.mylibbackend.api.OpenLibraryAPI;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.exceptions.UnexpectedStatusException;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.Book;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.repository.BookRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing book-related operations.
 * <p>
 * Provides access to book data from both the internal database and the external OpenLibrary API.
 * If a book is not found locally, the service attempts to retrieve it from OpenLibrary.
 * </p>
 *
 * <p>Supports pagination for retrieving lists of known (persisted) books.</p>
 *
 * @see BookRepository
 * @see OpenLibraryAPI
 */
@Service
@Slf4j
@AllArgsConstructor
public class BookService {
    private BookRepository bookRepository;
    private OpenLibraryAPI openLibraryAPI;

    /**
     * Retrieves a book by its OpenLibrary ID.
     * <p>
     * First checks the internal database. If the book is not found, it will attempt to fetch it from the OpenLibrary API.
     * </p>
     *
     * @param bookID The OpenLibrary book ID (e.g., "OL12345M")
     * @return An {@link Optional} containing the {@link Book}, or empty if not found in either source
     * @throws UnexpectedStatusException if the OpenLibrary API returns an unexpected status
     * @throws IOException if the external API call fails due to network issues
     */
    public Optional<Book> getBookById(String bookID) throws UnexpectedStatusException, IOException {
        List<Book> book = bookRepository.getBookByBookID(bookID);
        if(book.isEmpty()) {
            return openLibraryAPI.getBookByBookID(bookID);
        }
        return Optional.of(book.getFirst());
    }

    /**
     * Retrieves a paginated list of all books stored in the internal database.
     * <p>
     * These books are considered "known" and are typically associated with libraries or previously queried data.
     * </p>
     *
     * @param startIndex       the starting index (zero-based) for pagination
     * @param numResultsToGet  the number of results to return
     * @return a list of {@link Book} instances from the internal database
     */
    public List<Book> getAllKnownBooks(int startIndex, int numResultsToGet){
        return bookRepository.findAll(PageRequest.of(startIndex, numResultsToGet)).toList();
    }

}
