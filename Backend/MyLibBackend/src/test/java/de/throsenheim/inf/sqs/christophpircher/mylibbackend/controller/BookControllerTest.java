package de.throsenheim.inf.sqs.christophpircher.mylibbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.dto.*;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.*;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.service.BookService;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.service.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookService bookService;

    private static final String BOOK_ID = "OL123456M";
    private static final String BASE_URL = "/api/v1/books/";
    private static final String GETBOOK_URL = BASE_URL + "get/byID/" + BOOK_ID;
    private static final String GETALLBOOKS_URL = BASE_URL + "/get/all";
    private static final String USERNAME = "testuser";
    private static final String TEST_TITLE = "Test Book";
    private static final int DEFAULT_START_INDEX = 0;
    private static final int DEFAULT_NUM_RESULTS = 100;

    private UserPrincipal userPrincipal;
    private User testUser;

    private void injectCustomUserPrincipal(UserPrincipal principal) {
        TestingAuthenticationToken authentication =
                new TestingAuthenticationToken(principal, principal.getPassword(), "ROLE_USER"); // I need something here, even though I am not using rules
        authentication.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @BeforeEach
    void setUp() {
         testUser = User.builder()
                .id(UUID.randomUUID())
                .username(USERNAME)
                .passwordHash("hash")
                .build();
        userPrincipal = new UserPrincipal(testUser);
    }

    @Test
    void getBookByIdShouldReturnBookWithUserInfo() throws Exception {
        injectCustomUserPrincipal(userPrincipal);
        Book book = Book.builder().bookID(BOOK_ID).title(TEST_TITLE).build();
        when(bookService.getBookById(BOOK_ID)).thenReturn(Optional.of(book));
        when(bookService.getIndividualRating(any(), any())).thenReturn(4);
        when(bookService.getReadingStatus(any(), any())).thenReturn(ReadingStatus.READING);
        when(bookService.isBookInLibrary(any(), any())).thenReturn(true);
        when(bookService.isBookOnWishlist(any(), any())).thenReturn(false);

        mockMvc.perform(get(GETBOOK_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(TEST_TITLE))
                .andExpect(jsonPath("$.individualRating").value(4))
                .andExpect(jsonPath("$.readingStatus").value("READING"))
                .andExpect(jsonPath("$.bookIsInLibrary").value(true))
                .andExpect(jsonPath("$.bookIsOnWishlist").value(false));
    }

    @Test
    void getBookByIdShouldReturnBookWithoutUserInfoWhenUnauthenticated() throws Exception {
        // Arrange
        Book book = Book.builder().bookID(BOOK_ID).title(TEST_TITLE).build();
        when(bookService.getBookById(BOOK_ID)).thenReturn(Optional.of(book));

        // Act & Assert
        mockMvc.perform(get(GETBOOK_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookID").value(BOOK_ID))
                .andExpect(jsonPath("$.individualRating").value(0));
    }

    @Test
    void getBookByIdShouldReturnNotFoundWhenBookIsMissing() throws Exception {
        when(bookService.getBookById(BOOK_ID)).thenReturn(Optional.empty());

        mockMvc.perform(get(GETBOOK_URL))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllBooksShouldReturnBookList() throws Exception {
        Book book = Book.builder().bookID(BOOK_ID).title(TEST_TITLE).build();
        BookList list = BookList.builder().books(Collections.singletonList(book)).startIndex(DEFAULT_START_INDEX).numResults(1).skippedBooks(0).build();
        when(bookService.getAllKnownBooks(DEFAULT_START_INDEX, DEFAULT_NUM_RESULTS)).thenReturn(list);

        mockMvc.perform(get(GETALLBOOKS_URL))
                .andExpect(status().isOk());
    }

    @Test
    void addBookToLibraryShouldReturnCreated() throws Exception {
        injectCustomUserPrincipal(userPrincipal);
        AddBookRequestDTO dto = new AddBookRequestDTO(BOOK_ID);

        mockMvc.perform(post(BASE_URL + "add/library")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    void addBookToWishlistShouldReturnCreated() throws Exception {
        injectCustomUserPrincipal(userPrincipal);
        AddBookRequestDTO dto = new AddBookRequestDTO(BOOK_ID);

        mockMvc.perform(post(BASE_URL + "add/wishlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    void deleteBookFromWishlistShouldReturnOk() throws Exception {
        injectCustomUserPrincipal(userPrincipal);
        mockMvc.perform(delete(BASE_URL + "delete/wishlist/" + BOOK_ID))
                .andExpect(status().isOk());
    }

    @Test
    void deleteBookFromLibraryShouldReturnOk() throws Exception {
        injectCustomUserPrincipal(userPrincipal);
        mockMvc.perform(delete(BASE_URL + "delete/library/" + BOOK_ID))
                .andExpect(status().isOk());
    }

    @Test
    void updateBookRatingShouldReturnOk() throws Exception {
        injectCustomUserPrincipal(userPrincipal);
        ChangeBookRatingDTO dto = new ChangeBookRatingDTO(BOOK_ID, 5);

        mockMvc.perform(put(BASE_URL + "update/rating")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void updateReadingStatusShouldReturnOk() throws Exception {
        injectCustomUserPrincipal(userPrincipal);
        ChangeBookReadingStatusRequestDTO dto = new ChangeBookReadingStatusRequestDTO(BOOK_ID, ReadingStatus.READ);

        mockMvc.perform(put(BASE_URL + "update/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void getAllBooksInLibraryShouldReturnPaginatedListWithUserData() throws Exception {
        Book book = Book.builder().bookID(BOOK_ID).title(TEST_TITLE).build();
        BookList mockList = new BookList(1, 0, List.of(book), 0);

        when(bookService.getAllBooksInLibrary(0, 100, testUser)).thenReturn(mockList);
        when(bookService.getIndividualRating(BOOK_ID, testUser)).thenReturn(5);
        when(bookService.getReadingStatus(BOOK_ID, testUser)).thenReturn(ReadingStatus.UNREAD);
        when(bookService.isBookInLibrary(BOOK_ID, testUser)).thenReturn(true);
        when(bookService.isBookOnWishlist(BOOK_ID, testUser)).thenReturn(false);

        injectCustomUserPrincipal(userPrincipal); // helper to simulate auth context

        mockMvc.perform(get(BASE_URL + "get/library"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.books.length()").value(1))
                .andExpect(jsonPath("$.books[0].title").value(TEST_TITLE))
                .andExpect(jsonPath("$.books[0].individualRating").value(5))
                .andExpect(jsonPath("$.books[0].readingStatus").value("UNREAD"))
                .andExpect(jsonPath("$.books[0].bookIsInLibrary").value(true))
                .andExpect(jsonPath("$.books[0].bookIsOnWishlist").value(false));
    }

    @Test
    void getAllBooksOnWishlistShouldReturnPaginatedWishlistBooks() throws Exception {
        Book book = Book.builder().bookID(BOOK_ID).title(TEST_TITLE).build();
        BookList mockList = new BookList(1, 0, List.of(book), 0);

        when(bookService.getAllBooksOnWishlist(0, 100, testUser)).thenReturn(mockList);

        injectCustomUserPrincipal(userPrincipal); // ensure the same mock user

        mockMvc.perform(get(BASE_URL + "get/wishlist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.books.length()").value(1))
                .andExpect(jsonPath("$.books[0].title").value(TEST_TITLE));
    }


}