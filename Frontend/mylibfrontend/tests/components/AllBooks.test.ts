import { describe, it, vi, expect } from 'vitest';
import { computed, nextTick, ref } from 'vue';
import { mount } from '@vue/test-utils';
import AllBooks from '../../src/components/AllBooks.vue';
import { createBookDTO } from '../factories/book';

// Define the mock ref first (MUST be before vi.mock)
const isAuthenticatedRef = ref(true);

// Properly mock the module using the ref
vi.mock('../../src/wrapper/AuthInfoWrapper', () => ({
    isAuthenticated: computed(() => isAuthenticatedRef.value),
    __esModule: true,
}));

// Helper to wrap books in BookListDTO structure
const createBookListDTO = (books) => ({
    numResults: books.length,
    skippedBooks: 0,
    startIndex: 0,
    books,
});

// Helper to check if button with specific text exists
function hasButtonWithText(wrapper, text) {
    return wrapper.findAll('button').some((btn) => btn.text() === text);
}

describe('AllBooks.vue', () => {
    let wrapper;

    const mountComponent = () => {
        wrapper = mount(AllBooks);
    };

    it('renders average rating when present', async () => {
        const book = createBookDTO({ averageRating: 4.7 });
        mountComponent();
        wrapper.vm.bookList.books.value = createBookListDTO([book]);
        await nextTick();
        expect(wrapper.html()).toContain('4.7 / 5');
    });

    it('renders fallback when average rating is missing', async () => {
        const book = createBookDTO({ averageRating: undefined });
        mountComponent();
        wrapper.vm.bookList.books.value = createBookListDTO([book]);
        await nextTick();
        expect(wrapper.html()).toContain('Not rated yet');
    });

    it('shows "Add to Library" button when authenticated and book not in library', async () => {
        isAuthenticatedRef.value = true;
        const book = createBookDTO({ bookIsInLibrary: false });
        mountComponent();
        wrapper.vm.bookList.books.value = createBookListDTO([book]);
        await nextTick();
        expect(hasButtonWithText(wrapper, 'Add to Library')).toBe(true);
    });

    it('does not show "Add to Library" if not authenticated', async () => {
        isAuthenticatedRef.value = false;
        const book = createBookDTO({ bookIsInLibrary: false });
        mountComponent();
        wrapper.vm.bookList.books.value = createBookListDTO([book]);
        await nextTick();
        expect(hasButtonWithText(wrapper, 'Add to Library')).toBe(false);
    });

    it('shows "Add to Wishlist" if eligible (not in library and not on wishlist)', async () => {
        isAuthenticatedRef.value = true;
        const book = createBookDTO({ bookIsInLibrary: false, bookIsOnWishlist: false });
        mountComponent();
        wrapper.vm.bookList.books.value = createBookListDTO([book]);
        await nextTick();
        expect(hasButtonWithText(wrapper, 'Add to Wishlist')).toBe(true);
    });

    it('hides "Add to Wishlist" if already wished for', async () => {
        isAuthenticatedRef.value = true;
        const book = createBookDTO({ bookIsInLibrary: false, bookIsOnWishlist: true });
        mountComponent();
        wrapper.vm.bookList.books.value = createBookListDTO([book]);
        await nextTick();
        expect(hasButtonWithText(wrapper, 'Add to Wishlist')).toBe(false);
    });

    it('hides both buttons if book is already in library', async () => {
        isAuthenticatedRef.value = true;
        const book = createBookDTO({ bookIsInLibrary: true, bookIsOnWishlist: false });
        mountComponent();
        wrapper.vm.bookList.books.value = createBookListDTO([book]);
        await nextTick();
        expect(hasButtonWithText(wrapper, 'Add to Library')).toBe(false);
        expect(hasButtonWithText(wrapper, 'Add to Wishlist')).toBe(false);
    });
});
