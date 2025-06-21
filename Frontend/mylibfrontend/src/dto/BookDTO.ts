/**
 * Data Transfer Object (DTO) for representing book details in API responses.
 *
 * Encapsulates all relevant book metadata such as title, authors, ISBNs, cover images,
 * description, ratings, and user-specific info like reading status or library inclusion.
 */

import {ReadingStatus} from "./ReadingStatus.ts";
export interface BookDTO {
    /** OpenLibrary Book ID */
    bookID: string;

    /** Title of the book */
    title: string;

    /** Subtitle of the book (optional) */
    subtitle?: string;

    /** List of authors */
    authors: string[];

    /** Long-form book description (optional) */
    description?: string;

    /** List of ISBNs (10 or 13 digit numbers) */
    isbns?: string[];

    /** URL to small cover image */
    coverURLSmall?: string;

    /** URL to medium cover image */
    coverURLMedium?: string;

    /** URL to large cover image */
    coverURLLarge?: string;

    /** Date of first publication */
    publishDate?: string;

    /** User-specific individual rating (0–5) */
    individualRating: number;

    /** Average user rating (0–5, float) */
    averageRating: number;

    /** User's reading status (optional; needs enum defined) */
    readingStatus?: typeof ReadingStatus;

    /** Whether book is in user's library */
    bookIsInLibrary: boolean;

    /** Whether book is on user's wishlist */
    bookIsOnWishlist: boolean;
}