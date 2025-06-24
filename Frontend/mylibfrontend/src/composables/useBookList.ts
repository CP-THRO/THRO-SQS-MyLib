import { ref, computed } from 'vue';
import type { BookListDTO } from "../dto/BookListDTO.ts";

/**
 * Composable for managing a paginated book list.
 *
 * Handles state for loading, pagination, error tracking,
 * and integrates with an external data fetch function.
 */
export function useBookList(fetchFn: (startIndex: number, pageSize: number, ...args: any[]) => Promise<any>) {
    /** Holds the fetched book list data */
    const books = ref<BookListDTO | null>(null);

    /** Loading indicator */
    const loading = ref(false);

    /** Error message if fetch fails */
    const error = ref<string | null>(null);

    /** Available options for page size selection */
    const pageSizes = [5, 10, 25, 50];

    /** Number of results per page */
    const pageSize = ref(10);

    /** Current page number (1-based) */
    const currentPage = ref(1);

    /** Additional arguments passed to the fetch function */
    const extraArgs = ref<any[]>([]);

    /** Total number of pages (derived from result count and page size) */
    const totalPages = computed(() => {
        return books.value ? Math.ceil(books.value.numResults / pageSize.value) : 1;
    });

    /**
     * Loads book data using the fetch function.
     * Stores the result and handles errors and loading state.
     */
    const loadBooks = async (...args: any[]) => {
        loading.value = true;
        error.value = null;

        if (args.length > 0) extraArgs.value = args;

        const startIndex = (currentPage.value - 1) * pageSize.value;

        try {
            books.value = await fetchFn(startIndex, pageSize.value, ...extraArgs.value);
        } catch (e: any) {
            error.value = e.message ?? 'An error occurred';
        } finally {
            loading.value = false;
        }
    };

    /**
     * Initializes an empty book list (e.g., for reset or fallback state).
     */
    const emptyInitBooks = () => {
        books.value = {
            numResults: 0,
            skippedBooks: 0,
            books: [],
            startIndex: 0,
        };
    };

    /**
     * Advances to the next page, if available.
     */
    const nextPage = () => {
        if (currentPage.value < totalPages.value) {
            currentPage.value++;
            loadBooks();
        }
    };

    /**
     * Goes back to the previous page, if not on the first.
     */
    const prevPage = () => {
        if (currentPage.value > 1) {
            currentPage.value--;
            loadBooks();
        }
    };

    /**
     * Updates the number of results per page and reloads the first page.
     */
    const updatePageSize = (size: number) => {
        pageSize.value = size;
        currentPage.value = 1;
        loadBooks();
    };

    /**
     * Sets both the current page and page size without triggering reload.
     * For moving back to a site from the book list
     */
    const setPagination = (page: number, size: number) => {
        currentPage.value = page;
        pageSize.value = size;
    };

    return {
        books,
        loading,
        error,
        pageSizes,
        pageSize,
        currentPage,
        totalPages,
        nextPage,
        prevPage,
        updatePageSize,
        loadBooks,
        emptyInitBooks,
        setPagination,
    };
}