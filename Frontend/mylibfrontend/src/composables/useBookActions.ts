import {apiService} from "../api/ApiService.ts";

type LoadBooksFn = () => Promise<void>;
type LoadBooksWithKeywordFn = (keywords: string) => Promise<void>;

export function useBookActions(
    bookList: any,
    loadBooks: LoadBooksFn | LoadBooksWithKeywordFn,
    keywords?: () => string
) {
    const onAddToLibrary = async (bookID: string) => {
        bookList.error.value = null;
        try {
            await apiService.addBookToLibrary(bookID);
            if (keywords) {
                await (loadBooks as LoadBooksWithKeywordFn)(keywords());
            } else {
                await (loadBooks as LoadBooksFn)();
            }
        } catch (e: any) {
            bookList.error.value = e.message ?? 'Failed to add book to library';
        }
    };

    const onAddToWishlist = async (bookID: string) => {
        bookList.error.value = null;
        try {
            await apiService.addBookToWishlist(bookID);
            if (keywords) {
                await (loadBooks as LoadBooksWithKeywordFn)(keywords());
            } else {
                await (loadBooks as LoadBooksFn)();
            }
        } catch (e: any) {
            bookList.error.value = e.message ?? 'Failed to add book to wishlist';
        }
    };

    return { onAddToLibrary, onAddToWishlist };
}