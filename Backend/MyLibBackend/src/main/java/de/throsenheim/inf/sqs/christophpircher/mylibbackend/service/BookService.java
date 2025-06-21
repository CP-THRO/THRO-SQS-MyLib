package de.throsenheim.inf.sqs.christophpircher.mylibbackend.service;

import de.throsenheim.inf.sqs.christophpircher.mylibbackend.api.OpenLibraryAPI;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.exceptions.BookNotFoundException;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.exceptions.BookNotInLibraryException;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.exceptions.UnexpectedStatusException;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.*;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.repository.BookRepository;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.repository.LibraryBookRepository;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

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
    private OpenLibraryAPI openLibraryAPI;
    private UserRepository userRepository;

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
        Optional<Book> book = bookRepository.getBookByBookID(bookID);
        if(book.isEmpty()) {
            return openLibraryAPI.getBookByBookID(bookID);
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
     * @return a list of {@link Book} instances from the internal database
     */
    public List<Book> getAllKnownBooks(int startIndex, int numResultsToGet){
        return bookRepository.findAll(PageRequest.of(startIndex, numResultsToGet)).toList();
    }

    /**
     * Retrieves a paginated list of books in a user's library.
     *
     * @param startIndex starting index for pagination
     * @param numResultsToGet number of results to return
     * @param user the user whose library to query
     * @return list of books in the user's library
     */
    public List<Book> getAllBooksInLibrary(int startIndex, int numResultsToGet, User user){
        List<LibraryBook> libraryBooks =  libraryBookRepository.getLibraryBooksByUser(user, PageRequest.of(startIndex, numResultsToGet));
        List<Book> books = new ArrayList<>(libraryBooks.size());
        for(LibraryBook libraryBook : libraryBooks) {
            books.add(libraryBook.getBook());
        }
        return books;
    }

    /**
     * Retrieves a paginated list of books on a user's wishlist.
     *
     * @param startIndex starting index for pagination
     * @param numResultsToGet number of results to return
     * @param user the user whose wishlist to query
     * @return sublist of books currently on the wishlist
     */
    public List<Book> getAllBooksOnWishlist(int startIndex, int numResultsToGet, User user){
        User newestUser = userRepository.getUserById(user.getId());
        List<Book> books = new ArrayList<>(newestUser.getWishlistBooks());

        if(books.isEmpty()) {
            return books;
        }
        int toIndex = Math.min(startIndex + numResultsToGet, books.size());
        return  books.subList(startIndex, toIndex);
    }

    /**
     * Gets the user's personal rating for a given book.
     *
     * @param bookID the book's OpenLibrary ID
     * @param user the user
     * @return the rating, or 0 if not rated
     */
    public int getIndividualRating(String bookID, User user){
        Optional<Book> book = bookRepository.getBookByBookID(bookID);
        if(book.isEmpty()) {
            return 0;
        } else {
            LibraryBookKey libraryBookKey = new LibraryBookKey(book.get().getId(), user.getId());
            Optional<LibraryBook> libraryBooks = libraryBookRepository.getLibraryBooksById(libraryBookKey);
            return libraryBooks.map(LibraryBook::getRating).orElse(0);
        }
    }

    /**
     * Gets the user's reading status for a given book.
     *
     * @param bookID the book's OpenLibrary ID
     * @param user the user
     * @return the reading status, or {@code UNREAD} if none found
     */
    public ReadingStatus getReadingStatus(String bookID, User user){
        Optional<Book> book = bookRepository.getBookByBookID(bookID);
        if(book.isEmpty()) {
            return ReadingStatus.UNREAD;
        } else {
            LibraryBookKey libraryBookKey = new LibraryBookKey(book.get().getId(), user.getId());
            Optional<LibraryBook> libraryBooks = libraryBookRepository.getLibraryBooksById(libraryBookKey);
            return libraryBooks.map(LibraryBook::getReadingStatus).orElse(ReadingStatus.UNREAD);
        }
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
    public void addBookToLibrary(String bookID, User user) throws UnexpectedStatusException, IOException, BookNotFoundException {
        Book book = getOrCreateBook(bookID);

        LibraryBookKey libraryBookKey = new LibraryBookKey(book.getId(), user.getId());
        Optional<LibraryBook> libraryBookOptional = libraryBookRepository.getLibraryBooksById(libraryBookKey);
        if(libraryBookOptional.isEmpty()) {
            LibraryBook.LibraryBookBuilder  libraryBookBuilder = LibraryBook.builder();
            libraryBookBuilder.id(libraryBookKey).book(book).user(user).rating(0).readingStatus(ReadingStatus.UNREAD);
            libraryBookRepository.save(libraryBookBuilder.build());
        }
        removeBookFromWishlist(bookID, user); //Automatically remove the book from the wishlist after putting it in the library, since it does not make sense to have it on both.
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
    public void addBookToWishList(String bookID, User user) throws UnexpectedStatusException, IOException, BookNotFoundException {
        Book book = getOrCreateBook(bookID);
        User newestUser = userRepository.getUserById(user.getId()); //get latest object
        if(!newestUser.getWishlistBooks().contains(book)) {
            newestUser.getWishlistBooks().add(book);
            userRepository.save(newestUser);
            book.getWishlistUsers().add(newestUser);
            bookRepository.save(book);
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
    public void rateBook(String bookID, User user, int rating) throws BookNotFoundException, BookNotInLibraryException {
        LibraryBook libraryBook = getBookFromLibrary(bookID, user);
        libraryBook.setRating(rating);
        libraryBookRepository.save(libraryBook);
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
    public void updateReadingStatus(String bookID, User user, ReadingStatus readingStatus) throws BookNotFoundException, BookNotInLibraryException {
        LibraryBook libraryBook = getBookFromLibrary(bookID, user);
        libraryBook.setReadingStatus(readingStatus);
        libraryBookRepository.save(libraryBook);

    }

    /**
     * Removes a book from the user's wishlist if present.
     *
     * @param bookID the book's OpenLibrary ID
     * @param user the user
     */
    public void removeBookFromWishlist(String bookID, User user){
        Optional<Book> bookOptional = bookRepository.getBookByBookID(bookID);
        if(bookOptional.isPresent()) {
            Book book = bookOptional.get();
            User newestUser = userRepository.getUserById(user.getId()); //get latest object
            if(newestUser.getWishlistBooks().contains(book)) {
                newestUser.getWishlistBooks().remove(book);
                userRepository.save(newestUser);
                book.getWishlistUsers().remove(newestUser);
                bookRepository.save(book);
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
        try{
            getBookFromLibrary(bookID, user);
        } catch (BookNotInLibraryException | BookNotFoundException e) {
            return  false;
        }
        return true;
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
        if(book.isPresent()) {
            User newestUser = userRepository.getUserById(user.getId()); //get latest object
            return newestUser.getWishlistBooks().contains(book.get());
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
        if(book.isEmpty()) {
            throw new BookNotFoundException(String.format("Book with id %s not found in database", bookID));
        }
        LibraryBookKey libraryBookKey = new LibraryBookKey(book.get().getId(), user.getId());
        Optional<LibraryBook> libraryBookOptional = libraryBookRepository.getLibraryBooksById(libraryBookKey);
        if(libraryBookOptional.isEmpty()) {
            throw new BookNotInLibraryException(String.format("Book with id %s not found in library of user %s", bookID, user.getUsername()));
        }
        return libraryBookOptional.get();
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
    private Book getOrCreateBook(String bookID) throws UnexpectedStatusException, IOException, BookNotFoundException {

        Optional<Book> bookOptional = bookRepository.getBookByBookID(bookID);
        Book book = null;
        boolean bookExistsInDatabase = bookOptional.isPresent();
        if(!bookExistsInDatabase) {
            bookOptional = openLibraryAPI.getBookByBookID(bookID);
            if(bookOptional.isEmpty()) {
                throw new BookNotFoundException(String.format("Book with ID \"%s\" not found", bookID));
            }
            book = bookOptional.get();
            book.setId(UUID.randomUUID());
            bookRepository.save(book);
        }else  {
            book = bookOptional.get();
        }
        return book;
    }

}
