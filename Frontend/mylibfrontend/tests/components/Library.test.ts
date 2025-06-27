import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount, flushPromises } from '@vue/test-utils';
import { createRouter, createMemoryHistory } from 'vue-router';

// 1) Define all mocks inside vi.mock factories or before them

vi.mock('../../src/api/ApiService', () => {
    const getLibraryMock = vi.fn().mockResolvedValue({
        books: [{
            bookID: 'b1',
            title: 'Sample Book',
            authors: ['Author A'],
            publishDate: '2020',
            readingStatus: 'UNREAD',
            individualRating: 3,
            averageRating: 4,
        }],
        numResults: 1,
    });
    return {
        apiService: {
            getLibrary: getLibraryMock,
        },
    };
});

vi.mock('../../src/composables/useBookList', () => {
    const booksData = [{
        bookID: 'b1',
        title: 'Sample Book',
        authors: ['Author A'],
        publishDate: '2020',
        individualRating: 3,
        averageRating: 4,
    }];
    return {
        useBookList: () => {
            const booksRef = { value: booksData }; // immediate population

            return {
                books: booksRef,
                loading: { value: false },
                error: { value: '' },
                currentPage: { value: 1 },
                totalPages: { value: 1 },
                pageSize: { value: 10 },
                pageSizes: [10, 20],
                nextPage: vi.fn(),
                prevPage: vi.fn(),
                updatePageSize: vi.fn(),
                loadBooks: vi.fn(),
            };
        },
    };
});
const mockDeleteFromLibrary = vi.fn();

vi.mock('../../src/composables/useBookActions', () => ({
    useBookActions: () => ({
        onDeleteFromLibrary: mockDeleteFromLibrary,
    }),
}));

vi.mock('../../src/composables/usePaginationState', () => ({
    usePaginationState: vi.fn(),
}));

vi.mock('../../src/wrapper/AuthInfoWrapper', () => ({
    isAuthenticated: true,
}));

vi.mock('../../src/components/BaseBookList.vue', () => {
    const { h } = require('vue');
    return {
        default: {
            name: 'BaseBookList',
            props: ['books', 'columns', 'loading', 'error', 'currentPage', 'totalPages', 'pageSize', 'pageSizes'],
            emits: ['next-page', 'prev-page', 'page-size-change'],
            setup(props, { slots }) {
                return () =>
                    h('table', { class: 'table' }, [
                        h('thead', [
                            h('tr', props.columns.map(col => h('th', col.label))),
                        ]),
                        h('tbody', props.books.map(book =>
                            h('tr', { key: book.bookID }, props.columns.map(col =>
                                h('td', slots[col.slot] ? slots[col.slot]({ book }) : book[col.field])
                            ))
                        )),
                    ]);
            }
        }
    };
});

// 2) Import component AFTER mocks

import LibraryBooks from '../../src/components/Library.vue';

// 3) Write tests

describe('LibraryBooks.vue', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        localStorage.setItem('is_authenticated', 'true');
    });

    it('renders the list of books with slots', async () => {
        const router = createRouter({
            history: createMemoryHistory(),
            routes: [{ path: '/login', component: { template: '<div>Login</div>' } }],
        });

        router.push('/');
        await router.isReady();

        const wrapper = mount(LibraryBooks, {
            global: { plugins: [router] },
        });

        await flushPromises();

        expect(wrapper.html()).toContain('Sample Book');
        expect(wrapper.html()).toContain('3 / 5');
        expect(wrapper.html()).toContain('4 / 5');
    });

    it('calls delete handler when delete button is clicked', async () => {
        const wrapper = mount(LibraryBooks);
        await flushPromises();

        const deleteBtn = wrapper.find('button.btn-danger');
        expect(deleteBtn.exists()).toBe(true);
        await deleteBtn.trigger('click');

        expect(mockDeleteFromLibrary).toHaveBeenCalledWith('b1');
    });

    it('redirects to login if not authenticated', async () => {
        localStorage.removeItem('is_authenticated');

        const router = createRouter({
            history: createMemoryHistory(),
            routes: [
                { path: '/', component: LibraryBooks },
                { path: '/login', component: { template: '<div>Login</div>' } },
            ],
        });

        router.push('/');
        await router.isReady();

        mount(LibraryBooks, {
            global: { plugins: [router] },
        });

        await flushPromises();
        expect(router.currentRoute.value.fullPath).toBe('/login');
    });
});
