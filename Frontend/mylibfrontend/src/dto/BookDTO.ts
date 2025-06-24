/**
 * Data Transfer Object (DTO) for book details in API responses.
 *
 * Includes metadata such as title, authors, ISBNs, cover images, descriptions,
 * ratings, and user-specific states like reading status or library inclusion.
 */

import { type ReadingStatusType } from "./ReadingStatus.ts";

export interface BookDTO {
    /** OpenLibrary book identifier (e.g., "OL9698350M") */
    bookID: string;

    /** Main title of the book */
    title: string;

    /** Subtitle of the book, if available */
    subtitle?: string;

    /** List of author names */
    authors: string[];

    /** Detailed book description, if available */
    description?: string;

    /** Array of ISBNs (10- or 13-digit) */
    isbns?: string[];

    /** URL to small-sized cover image */
    coverURLSmall?: string;

    /** URL to medium-sized cover image */
    coverURLMedium?: string;

    /** URL to large-sized cover image */
    coverURLLarge?: string;

    /** Original publication date (ISO 8601 format preferred) */
    publishDate?: string;

    /** User's personal rating (0–5 stars) */
    individualRating: number;

    /** Aggregated average rating from all users (0–5, decimal) */
    averageRating: number;

    /** User's reading status (e.g., 'reading', 'finished', etc.) */
    readingStatus?: ReadingStatusType;

    /** True if the book is in the user's library */
    bookIsInLibrary: boolean;

    /** True if the book is on the user's wishlist */
    bookIsOnWishlist: boolean;
}