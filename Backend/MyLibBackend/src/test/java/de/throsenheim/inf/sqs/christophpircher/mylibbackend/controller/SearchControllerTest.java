package de.throsenheim.inf.sqs.christophpircher.mylibbackend.controller;

import de.throsenheim.inf.sqs.christophpircher.mylibbackend.exceptions.UnexpectedStatusException;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.Book;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.BookList;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.service.BookService;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.service.SearchService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SearchControllerTest {

    private static final String SEARCH_URL = "/api/v1/search/external/keyword";
    private static final String KEYWORDS = "keywords";
    private static final String START_INDEX = "startIndex";
    private static final String NUM_TO_GET = "numResultsToGet";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SearchService searchService;

    @MockitoBean
    private BookService bookService;

    @Test
    void searchExternalKeywordShouldReturnOk() throws Exception {
        BookList bookList = new BookList();
        Book book = Book.builder().title("java").build();
        bookList.setBooks(List.of(book));

        when(searchService.searchKeywordsExternal("java", 0, 100)).thenReturn(bookList);

        mockMvc.perform(get(SEARCH_URL)
                        .param(KEYWORDS, "java")
                        .param(START_INDEX, "0")
                        .param(NUM_TO_GET, "100")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }


    @Test
    void searchExternalKeywordShouldHandleIOException() throws Exception {
        when(searchService.searchKeywordsExternal("java", 0, 100)).thenThrow(new IOException("Downstream error"));

        mockMvc.perform(get(SEARCH_URL)
                        .param(KEYWORDS, "java")
                        .param(START_INDEX, "0")
                        .param(NUM_TO_GET, "100")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadGateway());
    }

    @Test
    void searchExternalKeywordShouldHandleUnexpectedStatus() throws Exception {
        when(searchService.searchKeywordsExternal("java", 0, 100)).thenThrow(new UnexpectedStatusException("Unexpected status"));

        mockMvc.perform(get(SEARCH_URL)
                        .param(KEYWORDS, "java")
                        .param(START_INDEX, "0")
                        .param(NUM_TO_GET, "100")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadGateway());
    }
}
