import { onMounted, watch } from 'vue';
import { onBeforeRouteLeave } from 'vue-router';

/**
 * Manages pagination state using sessionStorage and route lifecycle.
 *
 * Restores pagination state on mount, persists changes,
 * and optionally cleans up state on route leave.
 */
export function usePaginationState(
    bookList: any,
    storageKey: string,
    loadBooksCallback: () => void,
    cleanupKeys: string[] = []
) {
    // Restore pagination state from sessionStorage on mount
    onMounted(() => {
        const saved = sessionStorage.getItem(storageKey);
        if (saved) {
            try {
                const { page, size } = JSON.parse(saved);
                bookList.setPagination(page ?? 1, size ?? bookList.pageSize.value);
            } catch {
                console.warn(`Invalid pagination data in sessionStorage for ${storageKey}`);
            }
        }
        loadBooksCallback();
    });

    // Persist pagination state when page or size changes
    watch(
        () => [bookList.currentPage.value, bookList.pageSize.value],
        ([page, size]) => {
            sessionStorage.setItem(storageKey, JSON.stringify({ page, size }));
        }
    );

    // Clean up saved pagination state when leaving the route (unless navigating to a book detail page)
    onBeforeRouteLeave((to) => {
        const isLeavingToBook = to.name === 'Book';
        if (!isLeavingToBook) {
            cleanupKeys.forEach(key => sessionStorage.removeItem(key));
        }
    });
}