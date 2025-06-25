package de.throsenheim.inf.sqs.christophpircher.mylibbackend.controller;

import de.throsenheim.inf.sqs.christophpircher.mylibbackend.dto.BookListDTO;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.Book;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.BookList;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.ReadingStatus;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.User;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.service.BookService;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.service.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UtilTest {

    private static final String BOOK_ID = "OL123456";
    private static final String BOOK_TITLE = "Test Book";

    @Mock
    private BookService bookService;

    @Mock
    private Authentication authentication;

    @Mock
    private UserPrincipal userPrincipal;

    @Mock
    private User user;

    private BookList bookList;

    @BeforeEach
    void setUp() {
        Book book = Book.builder()
                .bookID(BOOK_ID)
                .title(BOOK_TITLE)
                .build();

        bookList = BookList.builder()
                .books(List.of(book))
                .startIndex(0)
                .numResults(1)
                .skippedBooks(0)
                .build();
    }

    @Test
    void convertBookListToDTOWithUserSpecificInfoIfAuthenticatedShouldReturnGenericListWhenAnonymous() {
        // Provide a real instance of AnonymousAuthenticationToken
        Authentication anonymousAuth = new AnonymousAuthenticationToken(
                "key",
                "anonymousUser",
                List.of(() -> "ROLE_ANONYMOUS")
        );

        BookListDTO dto = Util.convertBookListToDTOWithUserSpecificInfoIfAuthenticated(bookList, bookService, anonymousAuth);

        assertEquals(1, dto.getBooks().size());
        assertEquals(BOOK_TITLE, dto.getBooks().getFirst().getTitle());
        assertEquals(0, dto.getBooks().getFirst().getIndividualRating());
    }

    @Test
    void convertBookListToDTOWithUserSpecificInfoIfAuthenticatedShouldReturnGenericListWhenAuthNull() {
        BookListDTO dto = Util.convertBookListToDTOWithUserSpecificInfoIfAuthenticated(bookList, bookService, null);

        assertEquals(1, dto.getBooks().size());
        assertEquals(BOOK_TITLE, dto.getBooks().getFirst().getTitle());
        assertEquals(0, dto.getBooks().getFirst().getIndividualRating());
    }

    @Test
    void convertBookListToDTOWithUserSpecificInfoIfAuthenticatedShouldReturnEnrichedList() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(userPrincipal.getUser()).thenReturn(user);

        when(bookService.getIndividualRating(BOOK_ID, user)).thenReturn(4);
        when(bookService.getReadingStatus(BOOK_ID, user)).thenReturn(ReadingStatus.READING);
        when(bookService.isBookInLibrary(BOOK_ID, user)).thenReturn(true);
        when(bookService.isBookOnWishlist(BOOK_ID, user)).thenReturn(false);

        BookListDTO dto = Util.convertBookListToDTOWithUserSpecificInfoIfAuthenticated(bookList, bookService, authentication);

        assertEquals(1, dto.getBooks().size());
        assertEquals(BOOK_TITLE, dto.getBooks().getFirst().getTitle());
        assertEquals(4, dto.getBooks().getFirst().getIndividualRating());
        assertEquals(ReadingStatus.READING, dto.getBooks().getFirst().getReadingStatus());
        assertTrue(dto.getBooks().getFirst().isBookIsInLibrary());
        assertFalse(dto.getBooks().getFirst().isBookIsOnWishlist());
    }

    @Test
    void convertBookListToDTOWithUserSpecificInfoIfAuthenticatedShouldReturnGenericListWhenUnauthenticated() {
        when(authentication.isAuthenticated()).thenReturn(false);

        BookListDTO dto = Util.convertBookListToDTOWithUserSpecificInfoIfAuthenticated(bookList, bookService, authentication);

        assertEquals(1, dto.getBooks().size());
        assertEquals(BOOK_TITLE, dto.getBooks().getFirst().getTitle());
    }
}