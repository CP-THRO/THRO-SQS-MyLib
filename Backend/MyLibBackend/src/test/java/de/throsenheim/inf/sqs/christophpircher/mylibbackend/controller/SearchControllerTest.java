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

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SearchService searchService;

    @MockitoBean
    private BookService bookService;

    @Test
    void searchExternalKeywordShouldReturnOk() throws Exception {
        // Arrange
        BookList bookList = new BookList();
        Book book = new Book(); // assuming a no-args constructor
        book.setTitle("Effective Java");
        bookList.setBooks(List.of(book)); // assuming BookList has an add() method

        when(searchService.searchKeywordsExternal("java", 0, 100)).thenReturn(bookList);

        // Act & Assert
        mockMvc.perform(get("/api/v1/search/external/keyword")
                        .param("keywords", "java")
                        .param("startIndex", "0")
                        .param("numResultsToGet", "100")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }


    @Test
    void searchExternalKeywordShouldHandleIOException() throws Exception {
        when(searchService.searchKeywordsExternal("java", 0, 100)).thenThrow(new IOException("Downstream error"));

        mockMvc.perform(get("/api/v1/search/external/keyword")
                        .param("keywords", "java")
                        .param("startIndex", "0")
                        .param("numResultsToGet", "100")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadGateway());
    }

    @Test
    void searchExternalKeywordShouldHandleUnexpectedStatus() throws Exception {
        when(searchService.searchKeywordsExternal("java", 0, 100)).thenThrow(new UnexpectedStatusException("Unexpected status"));

        mockMvc.perform(get("/api/v1/search/external/keyword")
                        .param("keywords", "java")
                        .param("startIndex", "0")
                        .param("numResultsToGet", "100")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadGateway());
    }
}
