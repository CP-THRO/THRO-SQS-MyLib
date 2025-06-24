/**
 * DTO for updating a user's rating of a specific book.
 *
 * Used in PUT requests to modify a book’s rating in the user’s library.
 * Both fields are required; rating must be an integer from 1 to 5.
 */
export interface ChangeBookRatingDTO {
    /**
     * OpenLibrary book identifier (e.g., "OL9698350M").
     */
    bookID: string;

    /**
     * User-assigned rating (integer between 1 and 5).
     */
    rating: number;
}