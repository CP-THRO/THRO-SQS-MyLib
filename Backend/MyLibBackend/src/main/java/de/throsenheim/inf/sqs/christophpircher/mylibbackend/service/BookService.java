package de.throsenheim.inf.sqs.christophpircher.mylibbackend.service;

import de.throsenheim.inf.sqs.christophpircher.mylibbackend.api.OpenLibraryAPI;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.exceptions.BookNotFoundException;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.exceptions.BookNotInLibraryException;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.exceptions.UnexpectedStatusException;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.*;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.repository.BookRepository;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.repository.LibraryBookRepository;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.repository.UserRepository;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.service.flyweights.ExternalBookFlyweightFactory;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service class for managing book-related operations for users and their personal libraries.
 * <p>
 * This service handles the retrieval, creation, and modification of books from both internal storage
 * and the external OpenLibrary API. It supports functionality for:
 * </p>
 * <ul>
 *   <li>Fetching books by ID</li>
 *   <li>Managing user libraries and wishlists</li>
 *   <li>Updating ratings and reading statuses</li>
 *   <li>Ensuring data consistency between local and external sources</li>
 * </ul>
 *
 * @see BookRepository
 * @see LibraryBookRepository
 * @see UserRepository
 * @see OpenLibraryAPI
 */
@Service
@Slf4j
@AllArgsConstructor
public class BookService {
    private BookRepository bookRepository;
    private LibraryBookRepository libraryBookRepository;
    private UserRepository userRepository;
    private ExternalBookFlyweightFactory externalBookFlyweightFactory;

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
        log.debug("Looking up book '{}' in local repository", bookID);
        Optional<Book> book = bookRepository.getBookByBookID(bookID);
        if (book.isEmpty()) {
            log.info("Book '{}' not found locally, querying OpenLibrary", bookID);
            return externalBookFlyweightFactory.getBookByID(bookID);
        }
        return book;
    }

    /**
     * Retrieves a paginated list of all books stored in the internal database.
     * <p>
     * These books are considered "known" and are typically associated with libraries or previously queried data.
     * </p>
     *
     * @param startIndex       the starting index (zero-based) for pagination
     * @param numResultsToGet  the number of results to return
     * @return A {@link BookList} instance with books from the internal database
     */
    public BookList getAllKnownBooks(int startIndex, int numResultsToGet) {
        log.debug("Fetching all known books with pagination: start={}, count={}", startIndex, numResultsToGet);
        List<Book> books = bookRepository.findAll(PageRequest.of(startIndex / numResultsToGet, numResultsToGet)).toList();
        int total = (int) bookRepository.count();
        log.info("Retrieved {} books (total available: {})", books.size(), total);
        return BookList.builder()
                .books(books)
                .numResults(total)
                .startIndex(startIndex)
                .skippedBooks(0)
                .build();
    }

    /**
     * Retrieves a paginated list of books in a user's library.
     *
     * @param startIndex starting index for pagination
     * @param numResultsToGet number of results to return
     * @param user the user whose library to query
     * @return BookList of books in the user's library
     */
    public BookList getAllBooksInLibrary(int startIndex, int numResultsToGet, User user) {
        log.debug("Fetching library books for user '{}' with pagination", user.getUsername());
        List<LibraryBook> libraryBooks = libraryBookRepository.getLibraryBooksByUser(user, PageRequest.of(startIndex / numResultsToGet, numResultsToGet));
        List<Book> books = libraryBooks.stream().map(LibraryBook::getBook).toList();
        long totalCount = libraryBookRepository.countByUser(user);
        log.info("User '{}' has {} books in library", user.getUsername(), totalCount);
        return BookList.builder()
                .books(books)
                .numResults((int) totalCount)
                .startIndex(startIndex)
                .skippedBooks(0)
                .build();
    }

    /**
     * Retrieves a paginated list of books on a user's wishlist.
     *
     * @param startIndex starting index for pagination
     * @param numResultsToGet number of results to return
     * @param user the user whose wishlist to query
     * @return BookList of books currently on the wishlist
     */
    public BookList getAllBooksOnWishlist(int startIndex, int numResultsToGet, User user) {
        log.debug("Fetching wishlist books for user '{}'", user.getUsername());
        User refreshedUser = userRepository.getUserById(user.getId());
        List<Book> wishlist = refreshedUser.getWishlistBooks().stream()
                .sorted((b1, b2) -> b1.getTitle().compareTo(b2.getTitle()))
                .toList();

        if (wishlist.isEmpty()) {
            log.info("User '{}' has an empty wishlist", user.getUsername());
        }

        int toIndex = Math.min(startIndex + numResultsToGet, wishlist.size());
        return BookList.builder()
                .books(wishlist.subList(startIndex, toIndex))
                .numResults(wishlist.size())
                .startIndex(startIndex)
                .skippedBooks(0)
                .build();
    }

    /**
     * Gets the user's personal rating for a given book.
     *
     * @param bookID the book's OpenLibrary ID
     * @param user the user
     * @return the rating, or 0 if not rated
     */
    public int getIndividualRating(String bookID, User user) {
        Optional<Book> book = bookRepository.getBookByBookID(bookID);
        if (book.isEmpty()) {
            log.debug("Rating lookup failed: book '{}' not found", bookID);
            return 0;
        }
        LibraryBookKey key = new LibraryBookKey(book.get().getId(), user.getId());
        return libraryBookRepository.getLibraryBooksById(key).map(LibraryBook::getRating).orElse(0);
    }

    /**
     * Gets the user's reading status for a given book.
     *
     * @param bookID the book's OpenLibrary ID
     * @param user the user
     * @return the reading status, or {@code UNREAD} if none found
     */
    public ReadingStatus getReadingStatus(String bookID, User user) {
        Optional<Book> book = bookRepository.getBookByBookID(bookID);
        if (book.isEmpty()) {
            log.debug("Reading status defaulted to UNREAD for non-existing book '{}'", bookID);
            return ReadingStatus.UNREAD;
        }
        LibraryBookKey key = new LibraryBookKey(book.get().getId(), user.getId());
        return libraryBookRepository.getLibraryBooksById(key).map(LibraryBook::getReadingStatus).orElse(ReadingStatus.UNREAD);
    }

    /**
     * Adds a book to the user's library. If the book does not exist locally, it will be fetched from OpenLibrary.
     * If the book is currently on the wishlist, it will be removed.
     *
     * @param bookID the book's OpenLibrary ID
     * @param user the user
     * @throws UnexpectedStatusException if OpenLibrary returns an unexpected status
     * @throws IOException if a network error occurs
     * @throws BookNotFoundException if the book cannot be found
     */
    @Transactional
    public void addBookToLibrary(String bookID, User user) throws UnexpectedStatusException, IOException, BookNotFoundException {
        log.info("User '{}' adding book '{}' to library", user.getUsername(), bookID);
        Book book = getOrCreateBook(bookID);
        LibraryBookKey key = new LibraryBookKey(book.getId(), user.getId());
        if (libraryBookRepository.getLibraryBooksById(key).isEmpty()) {
            LibraryBook libraryBook = LibraryBook.builder()
                    .id(key)
                    .book(book)
                    .user(user)
                    .rating(0)
                    .readingStatus(ReadingStatus.UNREAD)
                    .build();
            libraryBookRepository.save(libraryBook);
            log.info("Library entry created for user '{}' and book '{}'", user.getUsername(), bookID);
        } else {
            log.debug("Book '{}' already exists in user '{}' library", bookID, user.getUsername());
        }

        removeBookFromWishlist(bookID, user);
    }

    /**
     * Adds a book to the user's wishlist if it is not already present.
     *
     * @param bookID the book's OpenLibrary ID
     * @param user the user
     * @throws UnexpectedStatusException if OpenLibrary returns an unexpected status
     * @throws IOException if a network error occurs
     * @throws BookNotFoundException if the book cannot be found
     */
    @Transactional
    public void addBookToWishList(String bookID, User user) throws UnexpectedStatusException, IOException, BookNotFoundException {
        log.info("User '{}' adding book '{}' to wishlist", user.getUsername(), bookID);
        Book book = getOrCreateBook(bookID);
        User refreshedUser = userRepository.getUserById(user.getId());
        if (!refreshedUser.getWishlistBooks().contains(book)) {
            refreshedUser.getWishlistBooks().add(book);
            userRepository.save(refreshedUser);
            log.info("Book '{}' added to wishlist for user '{}'", bookID, user.getUsername());
        } else {
            log.debug("Book '{}' already on wishlist for user '{}'", bookID, user.getUsername());
        }
    }

    /**
     * Updates the user's rating for a book in their library.
     *
     * @param bookID the book's OpenLibrary ID
     * @param user the user
     * @param rating new rating (typically between 1 and 5)
     * @throws BookNotFoundException if the book doesn't exist
     * @throws BookNotInLibraryException if the book isn't in the user's library
     */
    @Transactional
    public void rateBook(String bookID, User user, int rating) throws BookNotFoundException, BookNotInLibraryException {
        log.info("User '{}' rating book '{}' with {}", user.getUsername(), bookID, rating);
        LibraryBook lb = getBookFromLibrary(bookID, user);
        lb.setRating(rating);
        libraryBookRepository.save(lb);
    }

    /**
     * Updates the user's reading status for a book in their library.
     *
     * @param bookID the book's OpenLibrary ID
     * @param user the user
     * @param readingStatus the new reading status
     * @throws BookNotFoundException if the book doesn't exist
     * @throws BookNotInLibraryException if the book isn't in the user's library
     */
    @Transactional
    public void updateReadingStatus(String bookID, User user, ReadingStatus status) throws BookNotFoundException, BookNotInLibraryException {
        log.info("User '{}' updating reading status for book '{}' to {}", user.getUsername(), bookID, status);
        LibraryBook lb = getBookFromLibrary(bookID, user);
        lb.setReadingStatus(status);
        libraryBookRepository.save(lb);
    }

    /**
     * Removes a book from the user's library if present.
     *
     * @param bookID the book's OpenLibrary ID
     * @param user the user
     */
    @Transactional
    public void removeBookFromLibrary(String bookID, User user) {
        try {
            LibraryBook lb = getBookFromLibrary(bookID, user);
            libraryBookRepository.delete(lb);
            log.info("Book '{}' removed from user '{}' library", bookID, user.getUsername());
        } catch (BookNotFoundException | BookNotInLibraryException e) {
            log.debug("Attempted to remove book '{}' from library, but it wasn't there for user '{}'", bookID, user.getUsername());
        }
    }


    /**
     * Removes a book from the user's wishlist if present.
     *
     * @param bookID the book's OpenLibrary ID
     * @param user the user
     */
    @Transactional
    public void removeBookFromWishlist(String bookID, User user) {
        Optional<Book> bookOptional = bookRepository.getBookByBookID(bookID);
        if (bookOptional.isPresent()) {
            User refreshedUser = userRepository.getUserById(user.getId());
            if (refreshedUser.getWishlistBooks().remove(bookOptional.get())) {
                userRepository.save(refreshedUser);
                log.info("Book '{}' removed from wishlist for user '{}'", bookID, user.getUsername());
            } else {
                log.debug("Book '{}' not found in wishlist for user '{}'", bookID, user.getUsername());
            }
        }
    }

    /**
     * Checks whether a book exists in the user's library.
     *
     * @param bookID the book's OpenLibrary ID
     * @param user the user
     * @return {@code true} if the book is in the library, {@code false} otherwise
     */
    public boolean isBookInLibrary(String bookID, User user) {
        try {
            getBookFromLibrary(bookID, user);
            return true;
        } catch (BookNotInLibraryException | BookNotFoundException e) {
            return false;
        }
    }

    /**
     * Checks whether a book is currently on the user's wishlist.
     *
     * @param bookID the book's OpenLibrary ID
     * @param user the user
     * @return {@code true} if on wishlist, {@code false} otherwise
     */
    public boolean isBookOnWishlist(String bookID, User user) {
        Optional<Book> book = bookRepository.getBookByBookID(bookID);
        if (book.isPresent()) {
            User refreshedUser = userRepository.getUserById(user.getId());
            return refreshedUser.getWishlistBooks().contains(book.get());
        }
        return false;
    }

    /**
     * Retrieves a book from the user's library, validating both book existence and ownership.
     *
     * @param bookID the book's OpenLibrary ID
     * @param user the user
     * @return the {@link LibraryBook} entry
     * @throws BookNotFoundException if the book doesn't exist
     * @throws BookNotInLibraryException if the user does not own this book
     */
    private LibraryBook getBookFromLibrary(String bookID, User user) throws BookNotFoundException, BookNotInLibraryException {
        Optional<Book> book = bookRepository.getBookByBookID(bookID);
        if (book.isEmpty()) {
            throw new BookNotFoundException("Book not found: " + bookID);
        }
        LibraryBookKey key = new LibraryBookKey(book.get().getId(), user.getId());
        return libraryBookRepository.getLibraryBooksById(key)
                .orElseThrow(() -> new BookNotInLibraryException("Book not in library: " + bookID));
    }

    /**
     * Retrieves a book from the database or fetches and saves it from OpenLibrary if not present.
     *
     * @param bookID the book's OpenLibrary ID
     * @return the resolved {@link Book}
     * @throws UnexpectedStatusException if OpenLibrary API responds abnormally
     * @throws IOException if a communication error occurs
     * @throws BookNotFoundException if the book does not exist in either source
     */
    @Transactional
    private Book getOrCreateBook(String bookID) throws UnexpectedStatusException, IOException, BookNotFoundException {
        Optional<Book> bookOpt = bookRepository.getBookByBookID(bookID);
        if (bookOpt.isPresent()) {
            return bookOpt.get();
        }

        log.info("Fetching book '{}' from external API", bookID);
        Optional<Book> external = externalBookFlyweightFactory.getBookByID(bookID);
        if (external.isEmpty()) {
            log.warn("Book '{}' not found externally", bookID);
            throw new BookNotFoundException("Book not found: " + bookID);
        }

        Book book = external.get();
        book.setId(UUID.randomUUID());
        bookRepository.save(book);
        log.info("Book '{}' saved to database from external source", bookID);
        return book;
    }

}
