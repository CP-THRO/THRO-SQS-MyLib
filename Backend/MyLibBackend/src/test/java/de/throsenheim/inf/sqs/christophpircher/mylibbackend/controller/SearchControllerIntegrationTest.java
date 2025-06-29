package de.throsenheim.inf.sqs.christophpircher.mylibbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;

import de.throsenheim.inf.sqs.christophpircher.mylibbackend.dto.BookDTO;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.dto.BookListDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class SearchControllerIntegrationTest {

    private static final String SEARCH_URL = "/api/v1/search/external/keyword";
    private static final String KEYWORDS = "keywords";
    private static final String START_INDEX = "startIndex";
    private static final String NUM_TO_GET = "numResultsToGet";

    // Inconsitencies at OpenLibrary
    private static final String BOOKID = "OL23106658M";
    private static final String BOOK_TITLE = "Mass Effect Ascension";
    private static final String AUTHOR = "Drew Karpyshyn";
    private static final String RELEASE_DATE = "2008";
    private static final String COVER_LARGE = "https://covers.openlibrary.org/b/id/12394458-L.jpg";
    private static final String COVER_MEDIUM = "https://covers.openlibrary.org/b/id/12394458-M.jpg";
    private static final String COVER_SMALL = "https://covers.openlibrary.org/b/id/12394458-S.jpg";

    private static final String KEYWORD_SEARCH = "Mass Effect";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private WireMockServer wireMockServer;

    private final BookDTO bookSearch = BookDTO.builder().bookID(BOOKID).title(BOOK_TITLE).authors(List.of(AUTHOR)).publishDate(RELEASE_DATE).isbns(null).coverURLLarge(COVER_LARGE).coverURLMedium(COVER_MEDIUM).coverURLSmall(COVER_SMALL).bookIsInLibrary(false).bookIsOnWishlist(false).averageRating(0).individualRating(0).build();

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(8089);
        wireMockServer.start();
    }

    @AfterEach
    void tearDown() {
        wireMockServer.resetAll();
        wireMockServer.stop();
    }


    @Test
    void searchExternalKeywordShouldReturnOk() throws Exception {

        wireMockServer.stubFor(WireMock.get(SEARCH_URL)
                .withQueryParam("q", equalTo(KEYWORD_SEARCH))
                .withQueryParam("offset", equalTo("0"))
                .withQueryParam("limit", equalTo("1"))
                .willReturn(aResponse().withBodyFile("search.json")));

            MvcResult result = mockMvc.perform(get(SEARCH_URL)
                            .param(KEYWORDS, KEYWORD_SEARCH)
                            .param(START_INDEX, "0")
                            .param(NUM_TO_GET, "1")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk()).andReturn();


            BookListDTO expectedBookListDTO = BookListDTO.builder().numResults(1).numResults(1).skippedBooks(0).books(List.of(bookSearch)).build();

            assertEquals(expectedBookListDTO, objectMapper.readValue(result.getResponse().getContentAsString(), BookListDTO.class));

        }


    @Test
    void searchExternalKeywordBadGateway() throws Exception {

        wireMockServer.stop();
        mockMvc.perform(get(SEARCH_URL)
                        .param(KEYWORDS, KEYWORD_SEARCH)
                        .param(START_INDEX, "0")
                        .param(NUM_TO_GET, "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadGateway());
    }
}
