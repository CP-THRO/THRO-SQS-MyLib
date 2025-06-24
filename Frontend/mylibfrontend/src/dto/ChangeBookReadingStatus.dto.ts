import {type ReadingStatusType} from "./ReadingStatus.ts";

/**
 * DTO used to update the reading status of a specific book in the user's personal library.
 *
 * Used in PUT requests to apply a new reading status, such as "READ", "READING", or "WISHLIST".
 */
export interface ChangeBookReadingStatusRequestDTO {
    /**
     * OpenLibrary book identifier (e.g., "OL9698350M").
     */
    bookID: string;

    /**
     * New reading status to apply to the book.
     */
    status: ReadingStatusType;
}