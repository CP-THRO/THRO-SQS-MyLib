// tests/composables/usePaginationState.test.ts
import { describe, it, vi, expect, beforeEach, afterEach } from 'vitest';
import { defineComponent, ref, nextTick } from 'vue';
import { mount } from '@vue/test-utils';
import { usePaginationState } from '../../composables/usePaginationState';
import { onBeforeRouteLeave } from 'vue-router';

vi.mock('vue-router', () => ({
    onBeforeRouteLeave: vi.fn(),
}));

describe('usePaginationState', () => {
    const storageKey = 'pagination_test_key';
    const cleanupKeys = [storageKey, 'another_key'];
    const loadBooksCallback = vi.fn();
    const mockSetPagination = vi.fn();

    const createBookList = () => ({
        currentPage: ref(1),
        pageSize: ref(10),
        setPagination: mockSetPagination,
    });

    const mountWithComposable = (bookList: any) => {
        return mount(defineComponent({
            setup() {
                usePaginationState(bookList, storageKey, loadBooksCallback, cleanupKeys);
                return () => null;
            }
        }));
    };

    beforeEach(() => {
        vi.clearAllMocks();
        sessionStorage.clear();
    });

    afterEach(() => {
        vi.clearAllMocks();
        sessionStorage.clear();
    });

    it('restores state from sessionStorage on mount', async () => {
        sessionStorage.setItem(storageKey, JSON.stringify({ page: 3, size: 25 }));
        const bookList = createBookList();

        mountWithComposable(bookList);
        await nextTick();

        expect(mockSetPagination).toHaveBeenCalledWith(3, 25);
        expect(loadBooksCallback).toHaveBeenCalled();
    });

    it('handles invalid sessionStorage JSON gracefully', async () => {
        sessionStorage.setItem(storageKey, 'invalid json');
        const warn = vi.spyOn(console, 'warn').mockImplementation(() => {});
        const bookList = createBookList();

        mountWithComposable(bookList);
        await nextTick();

        expect(warn).toHaveBeenCalledWith(expect.stringContaining(storageKey));
        expect(loadBooksCallback).toHaveBeenCalled();

        warn.mockRestore();
    });

    it('writes pagination state when currentPage or pageSize changes', async () => {
        const bookList = createBookList();
        mountWithComposable(bookList);
        await nextTick();

        bookList.currentPage.value = 2;
        await nextTick();
        expect(JSON.parse(sessionStorage.getItem(storageKey)!)).toEqual({ page: 2, size: 10 });

        bookList.pageSize.value = 50;
        await nextTick();
        expect(JSON.parse(sessionStorage.getItem(storageKey)!)).toEqual({ page: 2, size: 50 });
    });

    it('cleans up storage keys on route leave if not going to Book page', () => {
        const bookList = createBookList();
        mountWithComposable(bookList);

        const leaveCallback = (onBeforeRouteLeave as unknown as vi.Mock).mock.calls[0][0];

        sessionStorage.setItem(storageKey, 'some');
        sessionStorage.setItem('another_key', 'value');

        leaveCallback({ name: 'Home' });

        expect(sessionStorage.getItem(storageKey)).toBeNull();
        expect(sessionStorage.getItem('another_key')).toBeNull();
    });

    it('preserves storage keys if navigating to Book page', () => {
        const bookList = createBookList();
        mountWithComposable(bookList);

        const leaveCallback = (onBeforeRouteLeave as unknown as vi.Mock).mock.calls[0][0];

        sessionStorage.setItem(storageKey, 'some');
        sessionStorage.setItem('another_key', 'value');

        leaveCallback({ name: 'Book' });

        expect(sessionStorage.getItem(storageKey)).toBe('some');
        expect(sessionStorage.getItem('another_key')).toBe('value');
    });
});
