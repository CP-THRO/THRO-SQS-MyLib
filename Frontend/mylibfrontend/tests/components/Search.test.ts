import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount, flushPromises } from '@vue/test-utils';
import { ref } from 'vue';

const isAuthenticatedMock = ref(true);

// Declare spies here (but undefined)
let onAddToLibraryMock: ReturnType<typeof vi.fn>;
let onAddToWishlistMock: ReturnType<typeof vi.fn>;

// --- Mocks ---

vi.mock('../../src/wrapper/AuthInfoWrapper', () => ({
    get isAuthenticated() {
        return isAuthenticatedMock;
    },
}));

vi.mock('../../src/api/ApiService', () => ({
    apiService: {
        getKeywordSearch: vi.fn().mockResolvedValue({
            books: [
                {
                    bookID: 'b1',
                    title: 'Test Book 1',
                    authors: ['Author One'],
                    publishDate: '2020',
                    averageRating: 4.5,
                    bookIsInLibrary: false,
                    bookIsOnWishlist: false,
                },
                {
                    bookID: 'b2',
                    title: 'Test Book 2',
                    authors: ['Author Two'],
                    publishDate: '2021',
                    averageRating: 3.5,
                    bookIsInLibrary: true,
                    bookIsOnWishlist: false,
                },
            ],
            numResults: 2,
        }),
    },
}));

vi.mock('../../src/composables/useBookList', () => {
    const { ref } = require('vue');

    const books = ref([
        {
            bookID: 'b1',
            title: 'Test Book 1',
            authors: ['Author One'],
            publishDate: '2020',
            averageRating: 4.5,
            bookIsInLibrary: false,
            bookIsOnWishlist: false,
        },
        {
            bookID: 'b2',
            title: 'Test Book 2',
            authors: ['Author Two'],
            publishDate: '2021',
            averageRating: 3.5,
            bookIsInLibrary: true,
            bookIsOnWishlist: false,
        },
    ]);

    const loading = ref(false);
    const error = ref('');
    const currentPage = ref(1);
    const totalPages = ref(1);
    const pageSize = ref(10);
    const pageSizes = [10, 20];

    const loadBooks = vi.fn();
    const nextPage = vi.fn();
    const prevPage = vi.fn();
    const updatePageSize = vi.fn();
    const emptyInitBooks = vi.fn();

    return {
        useBookList: () => ({
            books,
            loading,
            error,
            currentPage,
            totalPages,
            pageSize,
            pageSizes,
            loadBooks,
            nextPage,
            prevPage,
            updatePageSize,
            emptyInitBooks,
        }),
    };
});

// Initialize spies here
onAddToLibraryMock = vi.fn();
onAddToWishlistMock = vi.fn();

vi.mock('../../src/composables/useBookActions', () => ({
    useBookActions: () => ({
        onAddToLibrary: onAddToLibraryMock,
        onAddToWishlist: onAddToWishlistMock,
    }),
}));

vi.mock('../../src/components/BaseBookList.vue', () => {
    const { h } = require('vue');
    return {
        default: {
            name: 'BaseBookList',
            props: [
                'books',
                'columns',
                'loading',
                'error',
                'currentPage',
                'totalPages',
                'pageSize',
                'pageSizes',
            ],
            emits: ['next-page', 'prev-page', 'page-size-change'],
            setup(props, { slots }) {
                return () =>
                    h('div', [
                        h('h3', 'BaseBookList Stub'),
                        h(
                            'ul',
                            (props.books ?? []).map((book) =>
                                h('li', { key: book.bookID }, [
                                    book.title,
                                    slots.actions ? slots.actions({ book }) : null,
                                ])
                            )
                        ),
                    ]);
            },
        },
    };
});

// Now import your component after mocks
import BookListComponent from '../../src/components/Search.vue';

// --- Tests ---

describe('BookListComponent.vue', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        isAuthenticatedMock.value = true;
        sessionStorage.clear();

        // Reset spies before each test
        onAddToLibraryMock.mockReset();
        onAddToWishlistMock.mockReset();
    });

    it('renders search input and initial book list', async () => {
        const wrapper = mount(BookListComponent);
        await flushPromises();

        expect(wrapper.find('input#inputKeywords').exists()).toBe(true);
        expect(wrapper.html()).toContain('Test Book 1');
        expect(wrapper.html()).toContain('Test Book 2');
    });

    it('calls loadBooks with keywords on search click and stores keywords in sessionStorage', async () => {
        const wrapper = mount(BookListComponent);
        await flushPromises();

        const input = wrapper.find('input#inputKeywords');
        expect(input.exists()).toBe(true);

        await input.setValue('vue testing');

        const button = wrapper.find('button.btn-primary');
        await button.trigger('click');

        expect(wrapper.vm.bookList.loadBooks).toHaveBeenCalledWith('vue testing');

        expect(sessionStorage.getItem('searchKeywords')).toBe('vue testing');
    });

    it('renders action buttons conditionally based on auth and book state', async () => {
        const wrapper = mount(BookListComponent);
        await flushPromises();

        const buttons = wrapper.findAll('button');
        expect(buttons.length).toBeGreaterThan(0);

        const addToLibraryBtn = buttons.find((btn) => btn.text() === 'Add to Library');
        const addToWishlistBtn = buttons.find((btn) => btn.text() === 'Add to Wishlist');

        expect(addToLibraryBtn).toBeTruthy();
        expect(addToWishlistBtn).toBeTruthy();
    });

    it('calls onAddToLibrary and onAddToWishlist handlers when buttons clicked', async () => {
        const wrapper = mount(BookListComponent);
        await flushPromises();

        const buttons = wrapper.findAll('button');
        const addToLibraryBtn = buttons.find((btn) => btn.text() === 'Add to Library');
        const addToWishlistBtn = buttons.find((btn) => btn.text() === 'Add to Wishlist');

        expect(addToLibraryBtn).toBeTruthy();
        expect(addToWishlistBtn).toBeTruthy();

        await addToLibraryBtn.trigger('click');
        await addToWishlistBtn.trigger('click');

        expect(onAddToLibraryMock).toHaveBeenCalledWith('b1');
        expect(onAddToWishlistMock).toHaveBeenCalledWith('b1');
    });

    it('reactively computes isAuthenticated', async () => {
        const wrapper = mount(BookListComponent);
        await flushPromises();

        expect(wrapper.vm.isAuthenticated.value).toBe(true);

        isAuthenticatedMock.value = false;
        await wrapper.vm.$nextTick();

        expect(wrapper.vm.isAuthenticated.value).toBe(false);
    });
});
