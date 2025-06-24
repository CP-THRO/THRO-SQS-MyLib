import { ref, computed} from 'vue';
import type {BookListDTO} from "../dto/BookListDTO.ts";

export function useBookList(fetchFn: (startIndex: number, pageSize: number, ...args: any[]) => Promise<any>) {
    const books = ref<BookListDTO | null>( null);
    const loading = ref(false);
    const error = ref<string | null>(null);

    const pageSizes = [5, 10, 25, 50];
    const pageSize = ref(10);
    const currentPage = ref(1);
    const extraArgs = ref<any[]>([]);

    const totalPages = computed(() => {
        return books.value ? Math.ceil(books.value.numResults / pageSize.value) : 1;
    });

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

    const emptyInitBooks = () =>{
        books.value = {numResults: 0, skippedBooks: 0, books: [], startIndex: 0}
    };

    const nextPage = () => {
        if (currentPage.value < totalPages.value) {
            currentPage.value++;
            loadBooks();
        }
    };

    const prevPage = () => {
        if (currentPage.value > 1) {
            currentPage.value--;
            loadBooks();
        }
    };

    const updatePageSize = (size: number) => {
        pageSize.value = size;
        currentPage.value = 1;
        loadBooks();
    };

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
