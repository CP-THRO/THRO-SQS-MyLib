import type { BookDTO } from "./BookDTO.ts";

/**
 * DTO for paginated book search results.
 *
 * Includes total result count, current page metadata,
 * returned books, and number of skipped entries.
 */
export interface BookListDTO {
    /**
     * Total number of matching results (across all pages).
     */
    numResults: number;

    /**
     * Zero-based index of the first result in this page.
     */
    startIndex: number;

    /**
     * List of books for the current page.
     */
    books: BookDTO[];

    /**
     * Number of results skipped due to invalid or incomplete data.
     */
    skippedBooks: number;
}