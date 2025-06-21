/**
 * Data Transfer Object (DTO) used for requests to add a book to a user's library or wishlist.
 *
 * This object contains the unique OpenLibrary book identifier needed to perform the operation.
 * Validation ensures that the book ID is not null, blank, or empty.
 */
export interface AddBookRequestDTO {
    /**
     * The OpenLibrary book identifier (e.g., "OL9698350M").
     *
     * This ID is required to locate and add the book to the user's collection or wishlist.
     */
    bookID: string;
}