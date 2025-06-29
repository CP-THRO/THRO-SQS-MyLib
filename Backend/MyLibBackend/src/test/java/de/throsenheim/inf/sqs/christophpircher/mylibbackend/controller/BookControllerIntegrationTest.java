package de.throsenheim.inf.sqs.christophpircher.mylibbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.dto.*;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.exceptions.UsernameExistsException;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.*;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.repository.BookRepository;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.repository.LibraryBookRepository;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.repository.UserRepository;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.service.flyweights.ExternalBookFlyweightFactory;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class BookControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private LibraryBookRepository  libraryBookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthController authController;

    @Autowired
    private ExternalBookFlyweightFactory externalBookFlyweightFactory;

    private WireMockServer wireMockServer;


    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "testpassword";
    private static final String BOOKID = "OL23106658M";
    private static final String GET_BOOK_BY_ID_URL = "/api/v1/books/get/byID/OL23106658M";
    private static final String ADD_BOOK_TO_LIBRARY_URL = "/api/v1/books/add/library";
    private static final String ADD_BOOK_TO_WISHLIST_URL = "/api/v1/books/add/wishlist";
    private static final String GET_LIBRARY_URL = "/api/v1/books/get/library";
    private static final String GET_WISHLIST_URL = "/api/v1/books/get/wishlist";
    private static final String UPDATE_RATING_URL = "/api/v1/books/update/rating";
    private static final String UPDATE_STATUS_URL = "/api/v1/books/update/status";
    private static final String GET_ALL_BOOKS_URL = "/api/v1/books/get/all";
    private static final String DELETE_BOOK_FROM_LIBRARY_URL = "/api/v1/books/delete/library/OL23106658M";
    private static final String DELETE_BOOK_FROM_WISHLIST_URL = "/api/v1/books/delete/wishlist/OL23106658M";

    private static final String EXTERNAL_GET_BOOK_URL = "/books/OL23106658M.json";
    private static final String EXTERNAL_GET_WORK_URL = "/works/OL5684854W.json";
    private static final String EXTERNAL_GET_AUTHOR_URL = "/authors/OL1385539A.json";

    private static final String BOOK_TITLE = "Mass effect";
    private static final String SUBTITLE = "Ascension";
    private static final String AUTHOR = "Drew Karpyshyn";
    private static final String DESCRIPTION = "Every advanced society in the galaxy relies on the technology of the Protheans, an ancient species that vanished fifty thousand years ago. After discovering a cache of Prothean technology on Mars in 2148, humanity is spreading to the stars; the newest interstellar species, struggling to carve out its place in the greater galactic community.\r\n\r\nOn the edge of colonized space, ship commander and Alliance war hero David Anderson investigates the remains of a top secret military research station; smoking ruins littered with bodies and unanswered questions. Who attacked this post and for what purpose? And where is Kahlee Sanders, the young scientist who mysteriously vanished from the base–hours before her colleagues were slaughtered?\r\n\r\nSanders is now the prime suspect, but finding her creates more problems for Anderson than it solves. Partnered with a rogue alien agent he can’t trust and pursued by an assassin he can’t escape, Anderson battles impossible odds on uncharted worlds to uncover a sinister conspiracy . . . one he won’t live to tell about. Or so the enemy thinks.";
    private static final String ISBN = "9780345498526";
    private static final String RELEASE_DATE = "2008";
    private static final String COVER_LARGE = "https://covers.openlibrary.org/b/id/12394458-L.jpg";
    private static final String COVER_MEDIUM = "https://covers.openlibrary.org/b/id/12394458-M.jpg";
    private static final String COVER_SMALL = "https://covers.openlibrary.org/b/id/12394458-S.jpg";

    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER = "Bearer ";

    private static final String CONTENT_TYPE = "Content-Type";


    private String jwtToken;

    private static BookDTO bookUnauthenticated;
    private static  BookDTO bookInLibrary;
    private static BookDTO bookOnWishlist;


    @BeforeAll
    static void beforeAll() {
        bookUnauthenticated = BookDTO.builder().bookID(BOOKID).title(BOOK_TITLE).subtitle(SUBTITLE).authors(List.of(AUTHOR)).description(DESCRIPTION).isbns(List.of(ISBN)).publishDate(RELEASE_DATE).coverURLLarge(COVER_LARGE).coverURLMedium(COVER_MEDIUM).coverURLSmall(COVER_SMALL).bookIsInLibrary(false).bookIsOnWishlist(false).averageRating(4).individualRating(0).build();
        bookInLibrary = BookDTO.builder().bookID(BOOKID).title(BOOK_TITLE).subtitle(SUBTITLE).authors(List.of(AUTHOR)).description(DESCRIPTION).isbns(List.of(ISBN)).publishDate(RELEASE_DATE).coverURLLarge(COVER_LARGE).coverURLMedium(COVER_MEDIUM).coverURLSmall(COVER_SMALL).bookIsInLibrary(true).bookIsOnWishlist(false).averageRating(4).individualRating(4).readingStatus(ReadingStatus.READ).build();
        bookOnWishlist = BookDTO.builder().bookID(BOOKID).title(BOOK_TITLE).subtitle(SUBTITLE).authors(List.of(AUTHOR)).description(DESCRIPTION).isbns(List.of(ISBN)).publishDate(RELEASE_DATE).coverURLLarge(COVER_LARGE).coverURLMedium(COVER_MEDIUM).coverURLSmall(COVER_SMALL).bookIsInLibrary(false).bookIsOnWishlist(true).averageRating(0).individualRating(0).readingStatus(ReadingStatus.UNREAD).build();
    }


    @BeforeEach
    @Transactional
    void setUp() throws UsernameExistsException {
        // Clear database

        wireMockServer = new WireMockServer(8089);
        wireMockServer.start();

        wireMockServer.stubFor(WireMock.get(urlPathEqualTo(EXTERNAL_GET_BOOK_URL))
                .willReturn(aResponse().withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).withBodyFile("OL23106658M.json")));

        wireMockServer.stubFor(WireMock.get(urlPathEqualTo(EXTERNAL_GET_WORK_URL))
                .willReturn(aResponse().withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).withBodyFile("OL5684854W.json")));

        wireMockServer.stubFor(WireMock.get(urlPathEqualTo(EXTERNAL_GET_AUTHOR_URL))
                .willReturn(aResponse().withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).withBodyFile("OL1385539A.json")));

        //Generate user and login
        AuthRequestDTO authRequestDTO = new AuthRequestDTO(USERNAME, PASSWORD);
        authController.addUser(authRequestDTO);
        jwtToken = authController.authenticate(authRequestDTO).getBody();


    }

    @AfterEach
    void tearDown() {
        wireMockServer.resetAll();
        wireMockServer.stop();

        libraryBookRepository.deleteAll(); // this must be deleted first due to foreign key constraint
        userRepository.deleteAll();
        bookRepository.deleteAll();

        externalBookFlyweightFactory.clearCache(); //Otherwise some 404 test do not work

    }


    @Test
    void getBookByIdShouldReturnBookWithUserInfo() throws Exception {
        addBookToUserLibraryAndChangeRatingAndStatus();
        MvcResult result = mockMvc.perform(get(GET_BOOK_BY_ID_URL).header(AUTHORIZATION, BEARER + jwtToken)).andExpect(status().isOk()).andReturn();
        assertEquals(objectMapper.writeValueAsString(bookInLibrary), result.getResponse().getContentAsString());
    }


    @Test
    void getBookByIdShouldReturnBookWithoutUserInfoWhenUnauthenticated() throws Exception {
        addBookToUserLibraryAndChangeRatingAndStatus();
        MvcResult result = mockMvc.perform(get(GET_BOOK_BY_ID_URL)).andExpect(status().isOk()).andReturn();
        assertEquals(objectMapper.writeValueAsString(bookUnauthenticated), result.getResponse().getContentAsString());
    }


    @Test
    void getBookByIdShouldReturnNotFoundWhenBookIsMissing() throws Exception {
        wireMockServer.resetAll();
        mockMvc.perform(get(GET_BOOK_BY_ID_URL)).andExpect(status().isNotFound()).andReturn();
    }


    @Test
    void getAllBooksShouldReturnBookList() throws Exception {
        addBookToUserLibraryAndChangeRatingAndStatus();
        MvcResult result = mockMvc.perform(get(GET_ALL_BOOKS_URL)).andExpect(status().isOk()).andReturn();
        BookListDTO expectedList = BookListDTO.builder().numResults(1).startIndex(0).skippedBooks(0).books(List.of(bookUnauthenticated)).build();
        assertEquals(objectMapper.writeValueAsString(expectedList), result.getResponse().getContentAsString());
    }

    @Test
    void getAllBooksShouldReturnBookListAuthenticated() throws Exception {
        addBookToUserLibraryAndChangeRatingAndStatus();
        MvcResult result = mockMvc.perform(get(GET_ALL_BOOKS_URL).header(AUTHORIZATION, BEARER + jwtToken)).andExpect(status().isOk()).andReturn();
        BookListDTO expectedList = BookListDTO.builder().numResults(1).startIndex(0).skippedBooks(0).books(List.of(bookInLibrary)).build();
        assertEquals(objectMapper.writeValueAsString(expectedList), result.getResponse().getContentAsString());
    }


    @Test
    void addBookToLibraryShouldReturnCreated() throws Exception {
        AddBookRequestDTO addBookRequestDTO = new AddBookRequestDTO(BOOKID);
        String json = objectMapper.writeValueAsString(addBookRequestDTO);
        mockMvc.perform(post(ADD_BOOK_TO_LIBRARY_URL)
                .header(AUTHORIZATION, BEARER + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)).andExpect(status().isCreated()).andReturn();

        User user = userRepository.getUserByUsername(USERNAME);
        Optional<Book> book = bookRepository.getBookByBookID(BOOKID);
        assertTrue(book.isPresent());
        LibraryBookKey key = new LibraryBookKey(book.get().getId(), user.getId());
        Optional<LibraryBook> libraryBook = libraryBookRepository.getLibraryBooksById(key);
        assertTrue(libraryBook.isPresent());
    }


    @Test
    @Transactional
    void addBookToWishlistShouldReturnCreated() throws Exception {
        AddBookRequestDTO addBookRequestDTO = new AddBookRequestDTO(BOOKID);
        String json = objectMapper.writeValueAsString(addBookRequestDTO);
        mockMvc.perform(post(ADD_BOOK_TO_WISHLIST_URL)
                .header(AUTHORIZATION, BEARER + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)).andExpect(status().isCreated()).andReturn();

        User user = userRepository.getUserByUsername(USERNAME);
        Optional<Book> book = bookRepository.getBookByBookID(BOOKID);
        assertTrue(book.isPresent());

        assert(user.getWishlistBooks().contains(book.get()));
    }

    @Test
    void addBookToLibraryUnauthenticated() throws Exception {
        AddBookRequestDTO addBookRequestDTO = new AddBookRequestDTO(BOOKID);
        String json = objectMapper.writeValueAsString(addBookRequestDTO);
        mockMvc.perform(post(ADD_BOOK_TO_LIBRARY_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)).andExpect(status().isForbidden()).andReturn();
    }

    @Test
    void addBookToWishlistUnauthenticated() throws Exception {
        AddBookRequestDTO addBookRequestDTO = new AddBookRequestDTO(BOOKID);
        String json = objectMapper.writeValueAsString(addBookRequestDTO);
        mockMvc.perform(post(ADD_BOOK_TO_WISHLIST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)).andExpect(status().isForbidden()).andReturn();
    }

    @Test
    void addBookToLibraryNotFound() throws Exception {
        wireMockServer.resetAll();
        AddBookRequestDTO addBookRequestDTO = new AddBookRequestDTO(BOOKID);
        String json = objectMapper.writeValueAsString(addBookRequestDTO);
        mockMvc.perform(post(ADD_BOOK_TO_LIBRARY_URL)
                        .header(AUTHORIZATION, BEARER + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)).andExpect(status().isNotFound()).andReturn();


    }

    @Test
    void addBookToWishlistNotFound() throws Exception {
        wireMockServer.resetAll();
        AddBookRequestDTO addBookRequestDTO = new AddBookRequestDTO(BOOKID);
        String json = objectMapper.writeValueAsString(addBookRequestDTO);
        mockMvc.perform(post(ADD_BOOK_TO_WISHLIST_URL)
                .header(AUTHORIZATION, BEARER + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)).andExpect(status().isNotFound()).andReturn();
    }


    @Test
    @Transactional
    void deleteBookFromWishlistShouldReturnOk() throws Exception {
        AddBookRequestDTO addBookRequestDTO = new AddBookRequestDTO(BOOKID);
        String json = objectMapper.writeValueAsString(addBookRequestDTO);
        mockMvc.perform(post(ADD_BOOK_TO_WISHLIST_URL)
                .header(AUTHORIZATION, BEARER + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)).andReturn();

        mockMvc.perform(delete(DELETE_BOOK_FROM_WISHLIST_URL).header(AUTHORIZATION, BEARER + jwtToken)).andExpect(status().isOk()).andReturn();

        User user = userRepository.getUserByUsername(USERNAME);
        assert(user.getWishlistBooks().isEmpty());
    }

    @Test
    void deleteBookFromWishlistUnauthenticated() throws Exception {
        mockMvc.perform(delete(DELETE_BOOK_FROM_WISHLIST_URL)).andExpect(status().isForbidden()).andReturn();
    }

    @Test
    void deleteBookFromLibraryShouldReturnOk() throws Exception {
        addBookToUserLibraryAndChangeRatingAndStatus();
        mockMvc.perform(delete(DELETE_BOOK_FROM_LIBRARY_URL).header(AUTHORIZATION, BEARER + jwtToken)).andExpect(status().isOk()).andReturn();

        User user = userRepository.getUserByUsername(USERNAME);
        List<LibraryBook> libraryBooks = libraryBookRepository.getLibraryBooksByUser(user, Pageable.ofSize(100));
        assertEquals(0, libraryBooks.size());
    }

    @Test
    void deleteBookFromLibraryUnauthorized() throws Exception {
        mockMvc.perform(delete(DELETE_BOOK_FROM_LIBRARY_URL)).andExpect(status().isForbidden()).andReturn();
    }




    @Test
    void updateBookRatingShouldReturnOk() throws Exception {
        addBookToLibrary();
        ChangeBookRatingDTO dto = new ChangeBookRatingDTO(BOOKID, 4);
        mockMvc.perform(put(UPDATE_RATING_URL).header(AUTHORIZATION, BEARER + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        Optional<Book> book = bookRepository.getBookByBookID(BOOKID);
        assertTrue(book.isPresent());
        User user = userRepository.getUserByUsername(USERNAME);

        LibraryBookKey key = new LibraryBookKey(book.get().getId(), user.getId());
        Optional<LibraryBook> libraryBook = libraryBookRepository.getLibraryBooksById(key);
        assertTrue(libraryBook.isPresent());
        assertEquals(4, libraryBook.get().getRating());

    }

    @Test
    void updateBookRatingNotFound() throws Exception {
        ChangeBookRatingDTO dto = new ChangeBookRatingDTO(BOOKID, 4);
        mockMvc.perform(put(UPDATE_RATING_URL).header(AUTHORIZATION, BEARER + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound()).andReturn();
    }

    @Test
    void updateBookRatingUnauthenticated() throws Exception {
        ChangeBookRatingDTO dto = new ChangeBookRatingDTO(BOOKID, 4);
        mockMvc.perform(put(UPDATE_RATING_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden()).andReturn();
    }



    @Test
    void updateReadingStatusShouldReturnOk() throws Exception {
        addBookToLibrary();
        ChangeBookReadingStatusRequestDTO dto = new ChangeBookReadingStatusRequestDTO(BOOKID, ReadingStatus.READING);
        mockMvc.perform(put(UPDATE_STATUS_URL).header(AUTHORIZATION, BEARER + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        Optional<Book> book = bookRepository.getBookByBookID(BOOKID);
        assertTrue(book.isPresent());
        User user = userRepository.getUserByUsername(USERNAME);

        LibraryBookKey key = new LibraryBookKey(book.get().getId(), user.getId());
        Optional<LibraryBook> libraryBook = libraryBookRepository.getLibraryBooksById(key);
        assertTrue(libraryBook.isPresent());
        assertEquals(ReadingStatus.READING, libraryBook.get().getReadingStatus());
    }

    @Test
    void updateReadingStatusNotFound() throws Exception {
        ChangeBookReadingStatusRequestDTO dto = new ChangeBookReadingStatusRequestDTO(BOOKID, ReadingStatus.READING);
        mockMvc.perform(put(UPDATE_STATUS_URL).header(AUTHORIZATION, BEARER + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound()).andReturn();
    }


    @Test
    void getAllBooksInLibraryShouldReturnPaginatedListWithUserData() throws Exception {
        addBookToUserLibraryAndChangeRatingAndStatus();
        MvcResult result = mockMvc.perform(get(GET_LIBRARY_URL).header(AUTHORIZATION, BEARER + jwtToken)).andExpect(status().isOk()).andReturn();

        BookListDTO  expected = BookListDTO.builder().numResults(1).startIndex(0).skippedBooks(0).books(List.of(bookInLibrary)).build();

        assertEquals(expected, objectMapper.readValue(result.getResponse().getContentAsString(), BookListDTO.class));
    }

    @Test
    void getAllBooksInLibraryUnauthenticated() throws Exception {
        mockMvc.perform(get(GET_LIBRARY_URL)).andExpect(status().isForbidden()).andReturn();
    }

    @Test
    void getAllBooksInWishlistShouldReturnPaginatedListWithUserData() throws Exception {
        AddBookRequestDTO addBookRequestDTO = new AddBookRequestDTO(BOOKID);
        String json = objectMapper.writeValueAsString(addBookRequestDTO);
        mockMvc.perform(post(ADD_BOOK_TO_WISHLIST_URL)
                .header(AUTHORIZATION, BEARER + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)).andReturn();

        MvcResult result = mockMvc.perform(get(GET_WISHLIST_URL).header(AUTHORIZATION, BEARER + jwtToken)).andExpect(status().isOk()).andReturn();

        BookListDTO  expected = BookListDTO.builder().numResults(1).startIndex(0).skippedBooks(0).books(List.of(bookOnWishlist)).build();

        assertEquals(expected, objectMapper.readValue(result.getResponse().getContentAsString(), BookListDTO.class));
    }

    @Test
    void getAllBooksInWishlistUnauthenticated() throws Exception {
        mockMvc.perform(get(GET_WISHLIST_URL)).andExpect(status().isForbidden()).andReturn();
    }

    private void addBookToUserLibraryAndChangeRatingAndStatus() throws Exception {
        addBookToLibrary();

        ChangeBookRatingDTO changeBookRatingDTO = new ChangeBookRatingDTO(BOOKID, 4);
        String json2 = objectMapper.writeValueAsString(changeBookRatingDTO);
        mockMvc.perform(put(UPDATE_RATING_URL)
                .header(AUTHORIZATION, BEARER + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                .content(json2)).andReturn();

        ChangeBookReadingStatusRequestDTO changeBookReadingStatusRequestDTO = new ChangeBookReadingStatusRequestDTO(BOOKID, ReadingStatus.READ);
        String json3 = objectMapper.writeValueAsString(changeBookReadingStatusRequestDTO);
        mockMvc.perform(put(UPDATE_STATUS_URL)
                .header(AUTHORIZATION, BEARER + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json3)).andReturn();
    }

    private void addBookToLibrary() throws Exception {
        AddBookRequestDTO addBookRequestDTO = new AddBookRequestDTO(BOOKID);
        String json = objectMapper.writeValueAsString(addBookRequestDTO);
        mockMvc.perform(post(ADD_BOOK_TO_LIBRARY_URL)
                .header(AUTHORIZATION, BEARER + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)).andReturn();
    }

}