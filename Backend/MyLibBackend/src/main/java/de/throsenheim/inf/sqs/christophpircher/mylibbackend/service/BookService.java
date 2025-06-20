package de.throsenheim.inf.sqs.christophpircher.mylibbackend.service;

import de.throsenheim.inf.sqs.christophpircher.mylibbackend.api.OpenLibraryAPI;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.exceptions.UnexpectedStatusException;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.Book;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.repository.BookRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableArgumentResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class BookService {
    private BookRepository bookRepository;
    private OpenLibraryAPI openLibraryAPI;

    /**
     * Get a book by its OpenLibrary API ID/key string. First searches for the book internally. If it cannot be found internally, it will try to get it from the OpenLibrary API
     * @param bookID OpenLibrary Book ID/key string of the book to get.
     * @return Optional with the book if the book has been found
     * @throws UnexpectedStatusException If the API returns an unexpected status
     * @throws IOException If something goes wrong with the connection
     */
    public Optional<Book> getBookById(String bookID) throws UnexpectedStatusException, IOException {
        List<Book> book = bookRepository.getBookByBookID(bookID);
        if(book.isEmpty()) {
            return openLibraryAPI.getBookByBookID(bookID);
        }
        return Optional.of(book.getFirst());
    }

    /**
     * Get a list with all books that are stored in the database, i.e. that are part in at least one Library
     * @param startIndex starting index from which to get the books. For pagination
     * @param numResultsToGet number of books to get starting from startIndex. For pagination
     * @return List with books
     */
    public List<Book> getAllKnownBooks(int startIndex, int numResultsToGet){
        return bookRepository.findAll(PageRequest.of(startIndex, numResultsToGet)).toList();
    }

}
