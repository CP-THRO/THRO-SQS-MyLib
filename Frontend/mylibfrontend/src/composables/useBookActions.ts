import { apiService } from '../api/ApiService';
import type { Ref } from 'vue';

type ErrorRef = { error: Ref<string | null> };
type ReloadFn = (...args: any[]) => Promise<void>;

/**
 * Composable providing book-related actions with unified error handling.
 *
 * Automatically resets errors, reloads data after each action,
 * and supports optional keyword-aware reloads (search).
 */
export function useBookActions(
    target: ErrorRef,              // Object containing a reactive error reference
    reload: ReloadFn,             // Function to reload the book list
    keywords?: () => string       // Optional keyword provider for contextual reload
) {
    /**
     * Executes an action with error handling and triggers a reload.
     */
    const withErrorHandling = async (fn: () => Promise<void>) => {
        target.error.value = null;
        try {
            await fn();
            await reload(keywords?.());
        } catch (e: any) {
            target.error.value = e.message || 'An error occurred';
        }
    };

    /**
     * Adds a book to the library.
     */
    const onAddToLibrary = async (bookID: string) =>
        withErrorHandling(() => apiService.addBookToLibrary(bookID));

    /**
     * Adds a book to the wishlist.
     */
    const onAddToWishlist = async (bookID: string) =>
        withErrorHandling(() => apiService.addBookToWishlist(bookID));

    /**
     * Removes a book from the library.
     */
    const onDeleteFromLibrary = async (bookID: string) =>
        withErrorHandling(() => apiService.deleteBookFromLibrary(bookID));

    /**
     * Removes a book from the wishlist.
     */
    const onDeleteFromWishlist = async (bookID: string) =>
        withErrorHandling(() => apiService.deleteBookFromWishlist(bookID));

    return {
        onAddToLibrary,
        onAddToWishlist,
        onDeleteFromLibrary,
        onDeleteFromWishlist,
    };
}
