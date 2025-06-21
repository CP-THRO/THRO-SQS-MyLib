import type {BookDTO} from "./BookDTO.ts";

/**
 * DTO for returning paginated book search results.
 *
 * Represents the result set from a search operation, including pagination metadata,
 * a list of books, and count of any skipped results due to data issues.
 */
export interface BookListDTO {
    /**
     * Total number of search results (not affected by pagination).
     */
    numResults: number;

    /**
     * Index of the first result in the current page (zero-based).
     */
    startIndex: number;

    /**
     * List of books returned for the current page.
     */
    books: BookDTO[];

    /**
     * Number of skipped books due to missing or malformed data (e.g., in OpenLibrary).
     */
    skippedBooks: number;
}