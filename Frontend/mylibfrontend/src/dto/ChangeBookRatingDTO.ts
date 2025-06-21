/**
 * DTO used to update a user's rating for a specific book.
 *
 * Typically used in PUT requests to update a book’s metadata in the user’s library.
 * Both fields are required and rating must be an integer between 1 and 5.
 */
export interface ChangeBookRatingDTO {
    /**
     * OpenLibrary book identifier (e.g. "OL9698350M").
     */
    bookID: string;

    /**
     * The user-assigned rating for the book (must be between 1 and 5).
     */
    rating: number;
}