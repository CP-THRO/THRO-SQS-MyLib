import { type ReadingStatusType } from "./ReadingStatus.ts";

/**
 * DTO for updating the reading status of a book in the user's library.
 *
 * Used in PUT requests to set a new status such as "READ", "READING", or "WISHLIST".
 */
export interface ChangeBookReadingStatusRequestDTO {
    /**
     * OpenLibrary book identifier (e.g., "OL9698350M").
     */
    bookID: string;

    /**
     * New reading status to assign to the book.
     */
    status: ReadingStatusType;
}
