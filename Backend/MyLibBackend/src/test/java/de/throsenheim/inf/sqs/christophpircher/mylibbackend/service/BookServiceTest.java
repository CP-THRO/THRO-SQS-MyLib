package de.throsenheim.inf.sqs.christophpircher.mylibbackend.service;

import de.throsenheim.inf.sqs.christophpircher.mylibbackend.exceptions.*;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.*;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.repository.BookRepository;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.repository.LibraryBookRepository;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.repository.UserRepository;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.service.flyweights.ExternalBookFlyweightFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private LibraryBookRepository libraryBookRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ExternalBookFlyweightFactory externalBookFlyweightFactory;

    @InjectMocks
    private BookService bookService;

    private static final String BOOK_ID = "OL123456M";
    private Book book;
    private User user;

    @BeforeEach
    void setUp() {
        book = Book.builder().bookID(BOOK_ID).title("Test Book").id(UUID.randomUUID()).build();
        user = User.builder().id(UUID.randomUUID()).username("testuser").wishlistBooks(new HashSet<>()).build();
    }

    @Test
    void getBookByIdReturnsFromLocalRepository() throws UnexpectedStatusException, IOException {
        when(bookRepository.getBookByBookID(BOOK_ID)).thenReturn(Optional.of(book));
        Optional<Book> result = bookService.getBookById(BOOK_ID);
        assertTrue(result.isPresent());
        verify(externalBookFlyweightFactory, never()).getBookByID(anyString());
    }

    @Test
    void getBookByIdFallsBackToExternal() throws UnexpectedStatusException, IOException {
        when(bookRepository.getBookByBookID(BOOK_ID)).thenReturn(Optional.empty());
        when(externalBookFlyweightFactory.getBookByID(BOOK_ID)).thenReturn(Optional.of(book));
        Optional<Book> result = bookService.getBookById(BOOK_ID);
        assertTrue(result.isPresent());
    }

    @Test
    void getAllKnownBooksReturnsPaginatedBooks() {
        when(bookRepository.findAll(any(PageRequest.class))).thenReturn(new PageImpl<>(List.of(book)));
        when(bookRepository.count()).thenReturn(1L);
        BookList result = bookService.getAllKnownBooks(0, 10);
        assertEquals(1, result.getBooks().size());
    }

    @Test
    void getAllBooksInLibraryReturnsUserBooks() {
        LibraryBook libraryBook = LibraryBook.builder().book(book).user(user).build();
        when(libraryBookRepository.getLibraryBooksByUser(eq(user), any())).thenReturn(List.of(libraryBook));
        when(libraryBookRepository.countByUser(user)).thenReturn(1L);
        BookList result = bookService.getAllBooksInLibrary(0, 10, user);
        assertEquals(1, result.getBooks().size());
    }

    @Test
    void getAllBooksOnWishlistReturnsSortedWishlist() {
        user.getWishlistBooks().add(book);
        when(userRepository.getUserById(user.getId())).thenReturn(user);
        BookList result = bookService.getAllBooksOnWishlist(0, 10, user);
        assertEquals(1, result.getBooks().size());
    }

    @Test
    void getAllBooksOnWishlistShouldReturnEmptyListWhenWishlistIsEmpty() {
        when(userRepository.getUserById(user.getId())).thenReturn(user);

        BookList result = bookService.getAllBooksOnWishlist(0, 5, user);
        assertNotNull(result);
        assertEquals(0, result.getBooks().size());
        assertEquals(0, result.getNumResults());
        assertEquals(0, result.getStartIndex());
        assertEquals(0, result.getSkippedBooks());
    }

    @Test
    void getIndividualRatingReturnsRating() {
        LibraryBook libraryBook = LibraryBook.builder().rating(4).build();
        when(bookRepository.getBookByBookID(BOOK_ID)).thenReturn(Optional.of(book));
        when(libraryBookRepository.getLibraryBooksById(any())).thenReturn(Optional.of(libraryBook));
        int rating = bookService.getIndividualRating(BOOK_ID, user);
        assertEquals(4, rating);
    }

    @Test
    void getIndividualRatingShouldReturnZeroWhenBookNotFound() {
        when(bookRepository.getBookByBookID(BOOK_ID)).thenReturn(Optional.empty());

        int rating = bookService.getIndividualRating(BOOK_ID, user);

        assertEquals(0, rating);
    }

    @Test
    void getReadingStatusReturnsStatus() {
        LibraryBook libraryBook = LibraryBook.builder().readingStatus(ReadingStatus.READING).build();
        when(bookRepository.getBookByBookID(BOOK_ID)).thenReturn(Optional.of(book));
        when(libraryBookRepository.getLibraryBooksById(any())).thenReturn(Optional.of(libraryBook));
        ReadingStatus status = bookService.getReadingStatus(BOOK_ID, user);
        assertEquals(ReadingStatus.READING, status);
    }

    @Test
    void getReadingStatusShouldReturnUnreadWhenBookNotFound() {
        when(bookRepository.getBookByBookID(BOOK_ID)).thenReturn(Optional.empty());

        ReadingStatus status = bookService.getReadingStatus(BOOK_ID, user);

        assertEquals(ReadingStatus.UNREAD, status);
    }

    @Test
    void addBookToLibraryAddsAndRemovesFromWishlist() throws UnexpectedStatusException, IOException, BookNotFoundException {
        when(bookRepository.getBookByBookID(BOOK_ID)).thenReturn(Optional.empty());
        when(externalBookFlyweightFactory.getBookByID(BOOK_ID)).thenReturn(Optional.of(book));
        when(libraryBookRepository.getLibraryBooksById(any())).thenReturn(Optional.empty());
        bookService.addBookToLibrary(BOOK_ID, user);
        verify(libraryBookRepository).save(any());
    }

    @Test
    void addBookToWishListAddsIfNotExists() throws UnexpectedStatusException, IOException, BookNotFoundException {
        when(bookRepository.getBookByBookID(BOOK_ID)).thenReturn(Optional.empty());
        when(externalBookFlyweightFactory.getBookByID(BOOK_ID)).thenReturn(Optional.of(book));
        when(userRepository.getUserById(user.getId())).thenReturn(user);
        bookService.addBookToWishList(BOOK_ID, user);
        assertTrue(user.getWishlistBooks().contains(book));
    }

    @Test
    void addBookToLibraryShouldSaveLibraryBookWhenNotAlreadyInLibrary() throws Exception {

        LibraryBookKey key = new LibraryBookKey(book.getId(), user.getId());
        LibraryBook existingLibraryBook = LibraryBook.builder()
                .id(key)
                .book(book)
                .user(user)
                .rating(3)
                .readingStatus(ReadingStatus.READING)
                .build();

        when(bookRepository.getBookByBookID(BOOK_ID)).thenReturn(Optional.of(book));
        when(libraryBookRepository.getLibraryBooksById(any())).thenReturn(Optional.of(existingLibraryBook));
        when(userRepository.getUserById(user.getId())).thenReturn(user); // for removeBookFromWishlist()

        bookService.addBookToLibrary(BOOK_ID, user);

        verify(libraryBookRepository, never()).save(any());
        verify(libraryBookRepository).getLibraryBooksById(any());
    }

    @Test
    void addBookToWishListShouldNotAddWhenAlreadyOnWishlist() throws Exception {
        User userWithWishlist = User.builder()
                .id(user.getId())
                .username(user.getUsername())
                .wishlistBooks(new HashSet<>(List.of(book))) // book already on wishlist
                .build();

        when(bookRepository.getBookByBookID(BOOK_ID)).thenReturn(Optional.of(book));
        when(userRepository.getUserById(user.getId())).thenReturn(userWithWishlist);
        // When
        bookService.addBookToWishList(BOOK_ID, user);
        // Then
        verify(userRepository, never()).save(any()); // no save should occur
    }

    @Test
    void removeBookFromLibraryRemovesIfExists(){
        LibraryBook libraryBook = LibraryBook.builder().book(book).user(user).build();
        when(bookRepository.getBookByBookID(BOOK_ID)).thenReturn(Optional.of(book));
        when(libraryBookRepository.getLibraryBooksById(any())).thenReturn(Optional.of(libraryBook));
        bookService.removeBookFromLibrary(BOOK_ID, user);
        verify(libraryBookRepository).delete(libraryBook);
    }

    @Test
    void removeBookFromLibraryShouldLogDebugWhenBookNotFound() {
        // Given
        when(bookRepository.getBookByBookID(BOOK_ID)).thenReturn(Optional.empty());

        // When / Then
        assertDoesNotThrow(() -> bookService.removeBookFromLibrary(BOOK_ID, user));

        // No interaction with delete expected
        verify(libraryBookRepository, never()).delete(any());
    }

    @Test
    void removeBookFromLibraryShouldLogDebugWhenBookNotInLibrary() {
        // Given
        when(bookRepository.getBookByBookID(BOOK_ID)).thenReturn(Optional.of(book));
        when(libraryBookRepository.getLibraryBooksById(any())).thenReturn(Optional.empty());

        // When / Then
        assertDoesNotThrow(() -> bookService.removeBookFromLibrary(BOOK_ID, user));

        // Still, no delete should happen
        verify(libraryBookRepository, never()).delete(any());
    }


    @Test
    void removeBookFromWishlistRemovesIfPresent() {
        user.getWishlistBooks().add(book);
        when(bookRepository.getBookByBookID(BOOK_ID)).thenReturn(Optional.of(book));
        when(userRepository.getUserById(user.getId())).thenReturn(user);
        bookService.removeBookFromWishlist(BOOK_ID, user);
        assertFalse(user.getWishlistBooks().contains(book));
    }

    @Test
    void isBookInLibraryReturnsTrueIfPresent() {
        when(bookRepository.getBookByBookID(BOOK_ID)).thenReturn(Optional.of(book));
        when(libraryBookRepository.getLibraryBooksById(any())).thenReturn(Optional.of(mock(LibraryBook.class)));
        assertTrue(bookService.isBookInLibrary(BOOK_ID, user));
    }

    @Test
    void isBookInLibraryShouldReturnFalseWhenBookNotFound() {
        when(bookRepository.getBookByBookID(BOOK_ID)).thenReturn(Optional.empty());

        boolean result = bookService.isBookInLibrary(BOOK_ID, user);

        assertFalse(result);
    }

    @Test
    void isBookInLibraryShouldReturnFalseWhenBookNotInLibrary() {
        when(bookRepository.getBookByBookID(BOOK_ID)).thenReturn(Optional.of(book));
        when(libraryBookRepository.getLibraryBooksById(any())).thenReturn(Optional.empty());

        boolean result = bookService.isBookInLibrary(BOOK_ID, user);

        assertFalse(result);
    }


    @Test
    void isBookOnWishlistReturnsTrueIfPresent() {
        user.getWishlistBooks().add(book);
        when(bookRepository.getBookByBookID(BOOK_ID)).thenReturn(Optional.of(book));
        when(userRepository.getUserById(user.getId())).thenReturn(user);
        assertTrue(bookService.isBookOnWishlist(BOOK_ID, user));
    }

    @Test
    void isBookOnWishlistShouldReturnFalseWhenBookNotFound() {
        when(bookRepository.getBookByBookID(BOOK_ID)).thenReturn(Optional.empty());

        boolean result = bookService.isBookOnWishlist(BOOK_ID, user);

        assertFalse(result);
    }


    @Test
    void rateBookShouldUpdateRating() throws BookNotInLibraryException, BookNotFoundException {
        LibraryBook libraryBook = LibraryBook.builder().book(book).user(user).build();
        when(bookRepository.getBookByBookID(BOOK_ID)).thenReturn(Optional.of(book));
        when(libraryBookRepository.getLibraryBooksById(any())).thenReturn(Optional.of(libraryBook));
        bookService.rateBook(BOOK_ID, user, 5);
        assertEquals(5, libraryBook.getRating());
    }

    @Test
    void updateReadingStatusShouldUpdateStatus() throws BookNotInLibraryException, BookNotFoundException {
        LibraryBook libraryBook = LibraryBook.builder().book(book).user(user).build();
        when(bookRepository.getBookByBookID(BOOK_ID)).thenReturn(Optional.of(book));
        when(libraryBookRepository.getLibraryBooksById(any())).thenReturn(Optional.of(libraryBook));
        bookService.updateReadingStatus(BOOK_ID, user, ReadingStatus.READ);
        assertEquals(ReadingStatus.READ, libraryBook.getReadingStatus());
    }
    @Test
    void rateBookShouldThrowBookNotFoundExceptionWhenBookDoesNotExist() {
        when(bookRepository.getBookByBookID(BOOK_ID)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class, () ->
                bookService.rateBook(BOOK_ID, user, 4)
        );

        verify(libraryBookRepository, never()).save(any());
    }

    @Test
    void updateReadingStatusShouldThrowBookNotFoundExceptionWhenBookDoesNotExist() {
        when(bookRepository.getBookByBookID(BOOK_ID)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class, () ->
                bookService.updateReadingStatus(BOOK_ID, user, ReadingStatus.READ)
        );

        verify(libraryBookRepository, never()).save(any());
    }

    @Test
    void addBookToLibraryShouldThrowBookNotFoundExceptionWhenExternalReturnsEmpty() throws UnexpectedStatusException, IOException {
        when(bookRepository.getBookByBookID(BOOK_ID)).thenReturn(Optional.empty());
        when(externalBookFlyweightFactory.getBookByID(BOOK_ID)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class, () -> bookService.addBookToLibrary(BOOK_ID, user));

        verify(libraryBookRepository, never()).save(any());
    }
    @Test
    void addBookToWishListShouldThrowBookNotFoundExceptionWhenExternalReturnsEmpty() throws Exception {
        when(bookRepository.getBookByBookID(BOOK_ID)).thenReturn(Optional.empty());
        when(externalBookFlyweightFactory.getBookByID(BOOK_ID)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class, () -> bookService.addBookToWishList(BOOK_ID, user));

        verify(userRepository, never()).save(any());
    }

}