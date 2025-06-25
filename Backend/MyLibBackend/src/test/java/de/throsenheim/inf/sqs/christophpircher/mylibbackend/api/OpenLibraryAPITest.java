package de.throsenheim.inf.sqs.christophpircher.mylibbackend.api;

import com.github.tomakehurst.wiremock.http.Fault;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.api.dto.OpenLibraryAPIAuthor;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.api.dto.OpenLibraryAPIEditions;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.api.dto.OpenLibraryAPIWork;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.exceptions.UnexpectedStatusException;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.Book;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.BookList;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class OpenLibraryAPIWireMockTest {

    private static final String SEARCH_PATH = "/search.json";
    private static final String GENERIC_BOOK_ID = "OL123456M";
    private static final String GENERIC_WORK_ID = "OL123456W";
    private static final String GENERIC_AUTHOR_ID = "OL123456A";
    private static final String OFFSET = "offset";
    private static final String LIMIT = "limit";
    private static final String GENERIC_BOOK_URL = "/books/OL123456M.json";
    private static final String GENERIC_WORK_URL = "/works/OL123456W.json";
    private static final String GENERIC_AUTHOR_URL = "/authors/OL123456A.json";
    private static final String GENERIC_EDITION_URL = "/works/OL123456W/editions.json";

    private WireMockServer wireMockServer;

    @Autowired
    private OpenLibraryAPI api;


    @BeforeEach
    void setup() {
        wireMockServer = new WireMockServer(8089); // or any test port
        wireMockServer.start();
    }

    @Test
    void testSearchWithCoverEditionKey() throws IOException, UnexpectedStatusException {
        // Given
        String query = "space";
        wireMockServer.stubFor(get(urlPathEqualTo(SEARCH_PATH))
                .withQueryParam("q", equalTo("space"))
                .withQueryParam(OFFSET, equalTo("0"))
                .withQueryParam(LIMIT, equalTo("1"))
                .willReturn(okJson("""
                    {
                      "numFound": 1,
                      "start": 0,
                      "docs": [
                        {
                          "title": "Space Odyssey",
                          "subtitle": "A Journey Beyond",
                          "cover_edition_key": "OL123456M",
                          "cover_i": 9999,
                          "author_name": ["Arthur C. Clarke"],
                          "first_publish_year": 1968
                        }
                      ]
                    }
                    """)));

        // When
        BookList result = api.searchBooks(query, 0, 1);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getNumResults());
        assertEquals(1, result.getBooks().size());
        assertEquals(GENERIC_BOOK_ID, result.getBooks().getFirst().getBookID());
        assertEquals("Space Odyssey", result.getBooks().getFirst().getTitle());
        assertEquals("A Journey Beyond", result.getBooks().getFirst().getSubtitle());
        assertEquals("Arthur C. Clarke", result.getBooks().getFirst().getAuthors().getFirst());
        assertEquals("1968", result.getBooks().getFirst().getPublishDate());
    }

    @Test
    void testSearchWithFallbackEditionKey() throws Exception {
        // Stub /search.json (missing cover_edition_key)
        wireMockServer.stubFor(get(urlPathEqualTo(SEARCH_PATH))
                .withQueryParam("q", equalTo("dune"))
                .withQueryParam(OFFSET, equalTo("0"))
                .withQueryParam(LIMIT, equalTo("1"))
                .willReturn(okJson("""
                {
                  "numFound": 1,
                  "start": 0,
                  "docs": [
                    {
                      "title": "Dune",
                      "subtitle": "The Desert Planet",
                      "cover_i": 101,
                      "author_name": ["Frank Herbert"],
                      "first_publish_year": 1965,
                      "key": "/works/OL123456W"
                    }
                  ]
                }
                """)));

        // Stub /works/{workID}/editions.json to provide fallback
        wireMockServer.stubFor(get(urlPathEqualTo(GENERIC_EDITION_URL))
                .willReturn(okJson("""
                {
                  "entries": [
                    { "key": "/books/OL123456M" }
                  ]
                }
                """)));

        // Act
        BookList result = api.searchBooks("dune", 0, 1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getBooks().size());

        var book = result.getBooks().getFirst();
        assertEquals(GENERIC_BOOK_ID, book.getBookID()); // fallback edition used
        assertEquals("Dune", book.getTitle());
        assertEquals("Frank Herbert", book.getAuthors().getFirst());
    }

    @Test
    void testSearchSkippedWhenNoCoverAndNoFallback() throws Exception {
        // Stub /search.json with work that has no cover_edition_key
        wireMockServer.stubFor(get(urlPathEqualTo(SEARCH_PATH))
                .withQueryParam("q", equalTo("lost"))
                .withQueryParam(OFFSET, equalTo("0"))
                .withQueryParam(LIMIT, equalTo("1"))
                .willReturn(okJson("""
                {
                  "numFound": 1,
                  "start": 0,
                  "docs": [
                    {
                      "title": "Lost Book",
                      "subtitle": "No Edition Key",
                      "cover_i": 202,
                      "author_name": ["Mystery Author"],
                      "first_publish_year": 1900,
                      "key": "/works/OL123456W"
                    }
                  ]
                }
                """)));

        // Stub /works/{workID}/editions.json with empty entries
        wireMockServer.stubFor(get(urlPathEqualTo(GENERIC_EDITION_URL))
                .willReturn(okJson("""
                {
                  "entries": []
                }
                """)));

        // Act
        BookList result = api.searchBooks("lost", 0, 1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getNumResults());
        assertEquals(0, result.getBooks().size()); // skipped due to no edition
        assertEquals(1, result.getSkippedBooks());
    }

    @Test
    void searchBooksShouldThrowUnexpectedStatusExceptionWhenBodyIsNull() {
        wireMockServer.stubFor(get(urlPathEqualTo(SEARCH_PATH))
                .withQueryParam("q", equalTo("test"))
                .withQueryParam(OFFSET, equalTo("0"))
                .withQueryParam(LIMIT, equalTo("10"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("null"))); // empty body

        assertThrows(UnexpectedStatusException.class, () -> api.searchBooks("test", 0, 10));
    }

    @Test
    void testSearchNon200ResponseThrowsException() {
        // Stub /search.json to return 500 Internal Server Error
        wireMockServer.stubFor(get(urlPathEqualTo(SEARCH_PATH))
                .withQueryParam("q", equalTo("invalid"))
                .withQueryParam(OFFSET, equalTo("0"))
                .withQueryParam(LIMIT, equalTo("1"))
                .willReturn(serverError()));

        // Act & Assert
        UnexpectedStatusException exception = assertThrows(
                UnexpectedStatusException.class,
                () -> api.searchBooks("invalid", 0, 1)
        );

        assertTrue(exception.getMessage().contains("Unexpected status code"));
    }

    @Test
    void testSearchIOExceptionThrowsWrappedIOException() {
        // Simulate network failure by delaying too long (or using a reset)
        wireMockServer.stubFor(get(urlPathEqualTo(SEARCH_PATH))
                .withQueryParam("q", equalTo("timeout"))
                .withQueryParam(OFFSET, equalTo("0"))
                .withQueryParam(LIMIT, equalTo("1"))
                .willReturn(aResponse()
                        .withFixedDelay(10_000))); // intentionally delay to simulate timeout

        // Act & Assert
        assertThrows(IOException.class, () -> api.searchBooks("timeout", 0, 1));
    }

    @Test
    void testGetBookByBookIDSuccessWithFullMetadata() throws Exception {
        // 1. Stub /books/{bookID}.json
        wireMockServer.stubFor(get(urlPathEqualTo(GENERIC_BOOK_URL))
                .willReturn(okJson("""
                {
                  "title": "Neuromancer",
                  "subtitle": "Cyberpunk Classic",
                  "publish_date": "1984",
                  "key": "/books/OL123456M",
                  "covers": [5555, 6666],
                  "isbn_10": ["0441569595"],
                  "isbn_13": ["9780441569595"],
                  "works": [ { "key": "/works/OL123456W" } ]
                }
                """)));

        // 2. Stub /works/{workID}.json
        wireMockServer.stubFor(get(urlPathEqualTo(GENERIC_WORK_URL))
                .willReturn(okJson("""
                {
                  "description": { "value": "A classic of the cyberpunk genre." },
                  "authors": [
                    { "author": { "key": "/authors/OL123456A" } }
                  ]
                }
                """)));

        // 3. Stub /authors/{authorID}.json
        wireMockServer.stubFor(get(urlPathEqualTo(GENERIC_AUTHOR_URL))
                .willReturn(okJson("""
                { "name": "William Gibson" }
                """)));

        // Act
        Optional<Book> result = api.getBookByBookID(GENERIC_BOOK_ID);

        // Assert
        assertTrue(result.isPresent());
        Book book = result.get();

        assertEquals("Neuromancer", book.getTitle());
        assertEquals("Cyberpunk Classic", book.getSubtitle());
        assertEquals("1984", book.getPublishDate());
        assertEquals(GENERIC_BOOK_ID, book.getBookID());
        assertEquals("A classic of the cyberpunk genre.", book.getDescription());
        assertEquals(List.of("William Gibson"), book.getAuthors());
        assertEquals(List.of("0441569595", "9780441569595"), book.getIsbns());

        assertTrue(book.getCoverURLSmall().contains("5555-S.jpg"));
    }

    @Test
    void testGetBookByBookIDSuccessWithoutCoverImage() throws Exception {
        // 1. Stub /books/{bookID}.json — no covers field
        wireMockServer.stubFor(get(urlPathEqualTo(GENERIC_BOOK_URL))
                .willReturn(okJson("""
                {
                  "title": "No Cover Book",
                  "subtitle": "Missing Cover ID",
                  "publish_date": "2000",
                  "key": "/books/OL123456M",
                  "isbn_10": ["0000000000"],
                  "isbn_13": ["9780000000000"],
                  "works": [ { "key": "/works/OL123456W" } ]
                }
                """)));

        // 2. Stub /works/{workID}.json
        wireMockServer.stubFor(get(urlPathEqualTo(GENERIC_WORK_URL))
                .willReturn(okJson("""
                {
                  "description": { "value": "No cover but still good." },
                  "authors": [
                    { "author": { "key": "/authors/OL123456A" } }
                  ]
                }
                """)));

        // 3. Stub /authors/{authorID}.json
        wireMockServer.stubFor(get(urlPathEqualTo(GENERIC_AUTHOR_URL))
                .willReturn(okJson("""
                { "name": "Author Without Cover" }
                """)));

        // Act
        Optional<Book> result = api.getBookByBookID(GENERIC_BOOK_ID);

        // Assert
        assertTrue(result.isPresent());
        Book book = result.get();

        assertEquals(GENERIC_BOOK_ID, book.getBookID());
        assertEquals("No cover but still good.", book.getDescription());
        assertEquals("Author Without Cover", book.getAuthors().getFirst());
        assertNull(book.getCoverURLSmall());  // No cover_i means no cover URLs
    }

    @Test
    void testGetBookByBookIDSuccessWithoutIsbns() throws Exception {
        // 1. Stub /books/{bookID}.json — no ISBNs
        wireMockServer.stubFor(get(urlPathEqualTo(GENERIC_BOOK_URL))
                .willReturn(okJson("""
                {
                  "title": "Book Without ISBNs",
                  "subtitle": "Unnumbered",
                  "publish_date": "1990",
                  "key": "/books/OL123456M",
                  "covers": [45678],
                  "works": [ { "key": "/works/OL123456W" } ]
                }
                """)));

        // 2. Stub /works/{workID}.json
        wireMockServer.stubFor(get(urlPathEqualTo(GENERIC_WORK_URL))
                .willReturn(okJson("""
                {
                  "description": { "value": "This book has no ISBNs." },
                  "authors": [
                    { "author": { "key": "/authors/OL123456A" } }
                  ]
                }
                """)));

        // 3. Stub /authors/{authorID}.json
        wireMockServer.stubFor(get(urlPathEqualTo(GENERIC_AUTHOR_URL))
                .willReturn(okJson("""
                { "name": "Unknown Author" }
                """)));

        // Act
        Optional<Book> result = api.getBookByBookID(GENERIC_BOOK_ID);

        // Assert
        assertTrue(result.isPresent());
        Book book = result.get();

        assertEquals(GENERIC_BOOK_ID, book.getBookID());
        assertEquals("Book Without ISBNs", book.getTitle());
        assertTrue(book.getIsbns().isEmpty(), "ISBN list should be empty");
    }

    @Test
    void testGetBookByBookIDNotFound() throws Exception {
        // Stub /books/OL404M.json to return 404
        wireMockServer.stubFor(get(urlPathEqualTo(GENERIC_BOOK_URL))
                .willReturn(aResponse().withStatus(404)));

        // Act
        Optional<Book> result = api.getBookByBookID(GENERIC_BOOK_ID);

        // Assert
        assertTrue(result.isEmpty(), "Expected empty Optional when book is not found");
    }

    @Test
    void getBookByBookIDShouldThrowUnexpectedStatusExceptionWhenBodyIsNull() {
        wireMockServer.stubFor(get(urlPathEqualTo(GENERIC_BOOK_URL))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("null"))); // empty body

        assertThrows(UnexpectedStatusException.class, () ->
                api.getBookByBookID(GENERIC_BOOK_ID));
    }

    @Test
    void testGetBookByBookIDServerError() {
        // Stub /books/OL500M.json to return HTTP 500
        wireMockServer.stubFor(get(urlPathEqualTo(GENERIC_BOOK_URL))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("Internal Server Error")));

        // Act & Assert
        UnexpectedStatusException ex = assertThrows(UnexpectedStatusException.class, () -> api.getBookByBookID(GENERIC_BOOK_ID));

        assertTrue(ex.getMessage().contains("Unexpected status code: 500"));
    }

    @Test
    void getBookByBookIDShouldThrowIOExceptionWhenConnectionFails() {
        // Arrange: stub that simulates a network failure (e.g. connection reset)
        wireMockServer.stubFor(get(urlEqualTo(GENERIC_BOOK_URL))
                .willReturn(aResponse()
                        .withFault(Fault.CONNECTION_RESET_BY_PEER)));

        // Act & Assert: method should throw IOException wrapped by our service
        assertThrows(IOException.class, () -> api.getBookByBookID(GENERIC_BOOK_ID));
    }


    @Test
    void getBookByBookIDShouldThrowUnexpectedStatusExceptionWhenWorkFetchIs500() {
        wireMockServer.stubFor(get(urlEqualTo(GENERIC_BOOK_URL))
                .willReturn(okJson("""
                    {
                      "title": "Test Book",
                      "key": "/books/OL123456M",
                      "works": [{ "key": "/works/OL123456W" }],
                      "isbn_10": [],
                      "isbn_13": [],
                      "covers": [123],
                      "subtitle": "A Subtitle",
                      "publish_date": "2020"
                    }
                """)));

        wireMockServer.stubFor(get(urlEqualTo(GENERIC_WORK_URL))
                .willReturn(aResponse().withStatus(500)));

        assertThrows(UnexpectedStatusException.class, () -> api.getBookByBookID(GENERIC_BOOK_ID));
    }

    @Test
    void getBookByBookIDShouldThrowIOExceptionWhenGetWorkByWorkIDFails() {
        wireMockServer.stubFor(get(urlEqualTo(GENERIC_BOOK_URL))
                .willReturn(okJson("""
                    {
                      "title": "Test Book",
                      "key": "/books/OL123456M",
                      "works": [{ "key": "/works/OL123456W" }],
                      "isbn_10": [],
                      "isbn_13": [],
                      "covers": [123],
                      "subtitle": "A Subtitle",
                      "publish_date": "2020"
                    }
                """)));

        wireMockServer.stubFor(get(urlEqualTo(GENERIC_WORK_URL))
                .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));

        assertThrows(IOException.class, () -> api.getBookByBookID(GENERIC_BOOK_ID));
    }

    @Test
    void getWorkByWorkIDShouldReturnWorkWhenResponseIsSuccessful() throws Exception {
        wireMockServer.stubFor(get(urlEqualTo(GENERIC_WORK_URL))
                .willReturn(okJson("""
                {
                  "description": "Sample Description",
                  "authors": []
                }
            """)));

        OpenLibraryAPIWork result = invokePrivateGetWorkByWorkID(api, GENERIC_WORK_ID);

        assertEquals("Sample Description", result.getDescription().getValue());
    }

    @Test
    void getWorkByWorkIDShouldThrowUnexpectedStatusExceptionWhenBodyIsNull() {
        wireMockServer.stubFor(get(urlEqualTo(GENERIC_WORK_URL))
                .willReturn(aResponse().withStatus(500))); // Simulate server error

        Exception exception = assertThrows(InvocationTargetException.class, () ->
                invokePrivateGetWorkByWorkID(api, GENERIC_WORK_ID));

        assertInstanceOf(UnexpectedStatusException.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("500"));
    }

    @Test
    void getWorkByWorkIDShouldThrowUnexpectedStatusExceptionWhenStatusIsNot200() {
        wireMockServer.stubFor(get(urlEqualTo(GENERIC_WORK_URL))
                .willReturn(aResponse().withStatus(500).withBody("Internal Server Error")));

        InvocationTargetException exception = assertThrows(InvocationTargetException.class, () ->
                invokePrivateGetWorkByWorkID(api, GENERIC_WORK_ID));

        assertInstanceOf(UnexpectedStatusException.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("500"));
    }

    @Test
    void getWorkByWorkIDShouldThrowIOExceptionWhenIOExceptionOccurs() {
        wireMockServer.stop(); // force IOException by shutting down WireMock

        InvocationTargetException exception = assertThrows(InvocationTargetException.class, () ->
                invokePrivateGetWorkByWorkID(api, GENERIC_WORK_ID));

        assertInstanceOf(IOException.class, exception.getCause());
    }

    @Test
    void getAuthorByAuthorIDShouldReturnAuthorWhenSuccessful() throws Exception {
        wireMockServer.stubFor(get(urlEqualTo(GENERIC_AUTHOR_URL))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                        {
                            "name": "Jane Doe"
                        }
                    """)));

        OpenLibraryAPIAuthor author = invokePrivateGetAuthorByAuthorID(api, GENERIC_AUTHOR_ID);

        assertNotNull(author);
        assertEquals("Jane Doe", author.getName());
    }

    @Test
    void getAuthorByAuthorIDShouldThrowUnexpectedStatusExceptionWhenStatusIsNotSuccessful() {
        wireMockServer.stubFor(get(urlEqualTo(GENERIC_AUTHOR_URL))
                .willReturn(aResponse().withStatus(500)));

        InvocationTargetException ex = assertThrows(InvocationTargetException.class, () ->
                invokePrivateGetAuthorByAuthorID(api, GENERIC_AUTHOR_ID));

        assertInstanceOf(UnexpectedStatusException.class, ex.getTargetException());
    }

    @Test
    void getAuthorByAuthorIDShouldThrowUnexpectedStatusExceptionWhenBodyIsNull() {
        wireMockServer.stubFor(get(urlEqualTo(GENERIC_AUTHOR_URL))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(""))); // empty body, different because of the DTO structure

        InvocationTargetException ex = assertThrows(InvocationTargetException.class, () ->
                invokePrivateGetAuthorByAuthorID(api, GENERIC_AUTHOR_ID));

        Throwable cause = ex.getTargetException();
        assertInstanceOf(IOException.class, cause);
    }


    @Test
    void getAuthorByAuthorIDShouldThrowIOExceptionWhenNetworkFails() {
        wireMockServer.stop(); // simulate connection failure

        InvocationTargetException ex = assertThrows(InvocationTargetException.class, () ->
                invokePrivateGetAuthorByAuthorID(api, GENERIC_AUTHOR_ID));

        Throwable cause = ex.getTargetException();
        assertInstanceOf(IOException.class, cause);
    }

    @Test
    void getWorkEditionsByIDShouldReturnEditionsWhenSuccessful() throws Exception {
        wireMockServer.stubFor(get(urlEqualTo(GENERIC_EDITION_URL))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                        {
                            "entries": [
                                { "key": "/books/OL123456M" },
                                { "key": "/books/OL67890M" }
                            ]
                        }
                    """)));

        OpenLibraryAPIEditions editions = invokePrivateGetWorkEditionsByID(api, GENERIC_WORK_ID);

        assertNotNull(editions);
        assertEquals(2, editions.getEditions().size());
        assertEquals(GENERIC_BOOK_ID, editions.getEditions().getFirst().getBookKeyWithoutURL());
    }

    @Test
    void getWorkEditionsByIDShouldThrowUnexpectedStatusExceptionWhenStatusIsNotSuccessful() {
        wireMockServer.stubFor(get(urlEqualTo(GENERIC_EDITION_URL))
                .willReturn(aResponse().withStatus(500)));

        InvocationTargetException ex = assertThrows(InvocationTargetException.class, () ->
                invokePrivateGetWorkEditionsByID(api, GENERIC_WORK_ID));

        assertInstanceOf(UnexpectedStatusException.class, ex.getTargetException());
    }
    @Test
    void getWorkEditionsByIDShouldThrowUnexpectedStatusExceptionWhenBodyIsNull() {
        wireMockServer.stubFor(get(urlEqualTo(GENERIC_EDITION_URL))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("null"))); // empty body

        InvocationTargetException ex = assertThrows(InvocationTargetException.class, () ->
                invokePrivateGetWorkEditionsByID(api, GENERIC_WORK_ID));

        Throwable cause = ex.getTargetException();
        assertInstanceOf(UnexpectedStatusException.class, cause);
    }


    @Test
    void getWorkEditionsByIDShouldThrowIOExceptionWhenNetworkFails() {
        wireMockServer.stop(); // simulate unreachable OpenLibrary API

        InvocationTargetException ex = assertThrows(InvocationTargetException.class, () ->
                invokePrivateGetWorkEditionsByID(api, GENERIC_WORK_ID));

        Throwable cause = ex.getTargetException();
        assertInstanceOf(IOException.class, cause);
    }

    @Test
    void alterIOExceptionShouldReturnPrefixedIOExceptionWithSameStacktrace() throws Exception {
        IOException original = new IOException("original message");

        IOException altered = invokePrivateAlterIOException(api, original);

        assertNotSame(original, altered);
        assertTrue(altered.getMessage().startsWith("OpenLibraryAPI: original message"));
        assertArrayEquals(original.getStackTrace(), altered.getStackTrace());
    }

    @Test
    void getCoverURLsShouldReturnCorrectFormattedURLs() throws Exception {
        int coverID = 12345;
        String[] urls = invokePrivateGetCoverURLs(api, coverID);

        assertEquals("https://covers.openlibrary.org/b/id/12345-S.jpg", urls[0]);
        assertEquals("https://covers.openlibrary.org/b/id/12345-M.jpg", urls[1]);
        assertEquals("https://covers.openlibrary.org/b/id/12345-L.jpg", urls[2]);
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    private OpenLibraryAPIWork invokePrivateGetWorkByWorkID(OpenLibraryAPI api, String workID) throws InaccessibleObjectException, SecurityException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        Method method = OpenLibraryAPI.class.getDeclaredMethod("getWorkByWorkID", String.class);
        method.setAccessible(true);
        return (OpenLibraryAPIWork) method.invoke(api, workID);
    }

    private OpenLibraryAPIAuthor invokePrivateGetAuthorByAuthorID(OpenLibraryAPI api, String authorID) throws InaccessibleObjectException, SecurityException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        Method method = OpenLibraryAPI.class.getDeclaredMethod("getAuthorByAuthorID", String.class);
        method.setAccessible(true);
        return (OpenLibraryAPIAuthor) method.invoke(api, authorID);
    }

    private OpenLibraryAPIEditions invokePrivateGetWorkEditionsByID(OpenLibraryAPI api, String workID) throws InaccessibleObjectException, SecurityException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        Method method = OpenLibraryAPI.class.getDeclaredMethod("getWorkEditionsByID", String.class);
        method.setAccessible(true);
        return (OpenLibraryAPIEditions) method.invoke(api, workID);
    }

    private IOException invokePrivateAlterIOException(OpenLibraryAPI api, IOException input) throws InaccessibleObjectException, SecurityException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        Method method = OpenLibraryAPI.class.getDeclaredMethod("alterIOException", IOException.class);
        method.setAccessible(true);
        return (IOException) method.invoke(api, input);
    }

    private String[] invokePrivateGetCoverURLs(OpenLibraryAPI api, int coverID) throws InaccessibleObjectException, SecurityException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        Method method = OpenLibraryAPI.class.getDeclaredMethod("getCoverURLs", int.class);
        method.setAccessible(true);
        return (String[]) method.invoke(api, coverID);
    }

}