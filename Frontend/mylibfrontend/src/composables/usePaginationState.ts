import { onMounted, watch } from 'vue';
import { onBeforeRouteLeave } from 'vue-router';

export function usePaginationState(
    bookList: any,
    storageKey: string,
    loadBooksCallback: () => void,
    additionalRestoreFn?: () => void,
    cleanupKeys: string[] = []
) {
    onMounted(() => {
        const saved = sessionStorage.getItem(storageKey);
        if (saved) {
            try {
                const { page, size } = JSON.parse(saved);
                additionalRestoreFn?.();
                bookList.setPagination(page || 1, size || bookList.pageSize.value);
            } catch {
                console.warn(`Invalid pagination data in sessionStorage for ${storageKey}`);
            }
        }
        loadBooksCallback();
    });

    watch(
        () => [bookList.currentPage.value, bookList.pageSize.value],
        ([page, size]) => {
            sessionStorage.setItem(storageKey, JSON.stringify({ page, size }));
        }
    );

    onBeforeRouteLeave((to) => {
        const isLeavingToBook = to.name === 'Book';
        if (!isLeavingToBook) {
            cleanupKeys.forEach(key => sessionStorage.removeItem(key));
        }
    });
}