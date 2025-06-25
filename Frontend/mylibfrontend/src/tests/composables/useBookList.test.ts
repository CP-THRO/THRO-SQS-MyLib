import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { nextTick } from 'vue';
import { useBookList } from '../../composables/useBookList';
import type { BookListDTO } from '../../dto/BookListDTO';

describe('useBookList', () => {
    let fetchFn: ReturnType<typeof vi.fn>;
    let composable: ReturnType<typeof useBookList>;

    const fakeBooks: BookListDTO = {
        numResults: 25,
        skippedBooks: 0,
        books: [{ bookID: 'OL1', title: 'Book', authors: ['Author'], bookIsInLibrary:false, bookIsOnWishlist:false, averageRating:0, individualRating:0 }],
        startIndex: 0,
    };

    beforeEach(() => {
        fetchFn = vi.fn();
        composable = useBookList(fetchFn);
    });

    afterEach(() => {
        vi.clearAllMocks();
    });

    it('loads books successfully', async () => {
        fetchFn.mockResolvedValue(fakeBooks);
        await composable.loadBooks('foo');

        expect(fetchFn).toHaveBeenCalledWith(0, 10, 'foo');
        expect(composable.books.value).toEqual(fakeBooks);
        expect(composable.error.value).toBeNull();
        expect(composable.loading.value).toBe(false);
    });

    it('handles fetch failure with message', async () => {
        fetchFn.mockRejectedValue(new Error('fail'));

        await composable.loadBooks();
        expect(composable.error.value).toBe('fail');
        expect(composable.books.value).toBeNull();
        expect(composable.loading.value).toBe(false);
    });

    it('handles fetch failure with no message', async () => {
        fetchFn.mockRejectedValue({});
        await composable.loadBooks();
        expect(composable.error.value).toBe('An error occurred');
    });

    it('computes totalPages from book data', async () => {
        fetchFn.mockResolvedValue({ ...fakeBooks, numResults: 26 }); // triggers 3 pages
        await composable.loadBooks();
        expect(composable.totalPages.value).toBe(3);
    });

    it('initializes empty book list', () => {
        composable.emptyInitBooks();
        expect(composable.books.value).toEqual({
            numResults: 0,
            skippedBooks: 0,
            books: [],
            startIndex: 0,
        });
    });

    it('moves to nextPage and loads', async () => {
        fetchFn.mockResolvedValue({ ...fakeBooks, numResults: 20 });
        await composable.loadBooks();
        composable.currentPage.value = 1;

        fetchFn.mockClear(); // clear previous call

        composable.nextPage();
        await nextTick();

        expect(composable.currentPage.value).toBe(2);
        expect(fetchFn).toHaveBeenCalled(); // confirms loadBooks ran
    });

    it('does not go to nextPage if on last page', async () => {
        fetchFn.mockResolvedValue({ ...fakeBooks, numResults: 10 }); // only 1 page
        await composable.loadBooks();
        const spy = vi.spyOn(composable, 'loadBooks');

        composable.nextPage();
        expect(composable.currentPage.value).toBe(1);
        expect(spy).not.toHaveBeenCalled();
    });

    it('moves to prevPage and loads', async () => {
        composable.currentPage.value = 2;

        fetchFn.mockClear(); // in case called before

        composable.prevPage();
        await nextTick();

        expect(composable.currentPage.value).toBe(1);
        expect(fetchFn).toHaveBeenCalled(); // confirms loadBooks ran
    });

    it('does not go to prevPage if on first page', () => {
        const spy = vi.spyOn(composable, 'loadBooks');
        composable.prevPage();
        expect(composable.currentPage.value).toBe(1);
        expect(spy).not.toHaveBeenCalled();
    });

    it('updates page size and resets currentPage', async () => {
        composable.currentPage.value = 5;

        fetchFn.mockClear();

        composable.updatePageSize(25);
        await nextTick();

        expect(composable.pageSize.value).toBe(25);
        expect(composable.currentPage.value).toBe(1);
        expect(fetchFn).toHaveBeenCalled(); // confirms loadBooks ran
    });

    it('sets pagination without triggering load', () => {
        const spy = vi.spyOn(composable, 'loadBooks');
        composable.setPagination(3, 25);

        expect(composable.currentPage.value).toBe(3);
        expect(composable.pageSize.value).toBe(25);
        expect(spy).not.toHaveBeenCalled();
    });
});