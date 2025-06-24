package de.throsenheim.inf.sqs.christophpircher.mylibbackend.controller;

import de.throsenheim.inf.sqs.christophpircher.mylibbackend.dto.BookDTO;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.dto.BookListDTO;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.Book;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.BookList;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.User;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.service.BookService;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.service.UserPrincipal;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.ArrayList;
import java.util.List;

class Util {

    private Util() {}


    /**
     * Converts a {@link BookList} into a {@link BookListDTO}, enriching each {@link BookDTO}
     * with user-specific information when the user is authenticated.
     * <p>
     * If the user is authenticated, the resulting {@link BookDTO}s will include:
     * individual ratings, reading status, and flags indicating whether the book
     * is part of the user's library or wishlist. If the user is not authenticated,
     * the method returns a {@link BookListDTO} with only the base information.
     * </p>
     *
     * @param bookList      the list of books to convert, including pagination metadata
     * @param bookService   the service used to retrieve user-specific book data
     * @param authentication the current authentication context, used to identify the user
     * @return a {@link BookListDTO} enriched with personalized data if the user is authenticated,
     *         or a generic DTO if not
     *
     * @see BookService#getIndividualRating(String, User)
     * @see BookService#getReadingStatus(String, User)
     * @see BookService#isBookInLibrary(String, User)
     * @see BookService#isBookOnWishlist(String, User)
     */
    static BookListDTO convertBookListToDTOWithUserSpecificInfoIfAuthenticated(BookList bookList, BookService bookService, Authentication authentication) {

        if (authentication != null &&
                authentication.isAuthenticated() &&
                !(authentication instanceof AnonymousAuthenticationToken)) {
            Object principal = authentication.getPrincipal();
            User user = ((UserPrincipal) principal).getUser();


            return convertBookListToDTOWithUserSpecificInfo(bookList, user, bookService);

        } else {
            return BookListDTO.fromSearchResult(bookList);
        }
    }

    /**
     * Converts a {@link BookList} into a {@link BookListDTO} with full enrichment
     * using user-specific information.
     * <p>
     * Each {@link BookDTO} will be populated with:
     * - the user's individual rating,
     * - current reading status,
     * - library inclusion flag,
     * - and wishlist status.
     * </p>
     * <p>
     * This method assumes the user is already authenticated and known.
     * </p>
     *
     * @param bookList    the list of books to convert
     * @param user        the authenticated user whose data will be used
     * @param bookService the service used to retrieve personalized metadata
     * @return a fully enriched {@link BookListDTO}
     */
    static BookListDTO convertBookListToDTOWithUserSpecificInfo(BookList bookList, User user, BookService bookService) {

        BookListDTO.BookListDTOBuilder bookListDTOBuilder = BookListDTO.builder();
        bookListDTOBuilder.startIndex(bookList.getStartIndex());
        bookListDTOBuilder.numResults(bookList.getNumResults());
        bookListDTOBuilder.skippedBooks(bookList.getSkippedBooks());
        List<BookDTO> bookDTOs = new ArrayList<>(bookList.getBooks().size());
        for (Book book : bookList.getBooks()) { //need cannot really use fromBook() method here, since I need to set some additional information
            BookDTO bookDTO = BookDTO.fromBook(book);
            bookDTO.setIndividualRating(bookService.getIndividualRating(book.getBookID(), user));
            bookDTO.setReadingStatus(bookService.getReadingStatus(book.getBookID(), user));
            bookDTO.setBookIsInLibrary(bookService.isBookInLibrary(book.getBookID(), user));
            bookDTO.setBookIsOnWishlist(bookService.isBookOnWishlist(book.getBookID(), user));
            bookDTOs.add(bookDTO);
        }
        bookListDTOBuilder.books(bookDTOs);
        return bookListDTOBuilder.build();
    }
}
