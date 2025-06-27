import { describe, it, vi, expect, beforeEach } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import { createRouter, createMemoryHistory } from 'vue-router';
import { ref } from 'vue';
import { ReadingStatus } from '../../src/dto/ReadingStatus';

const addToLibrarySpy = vi.fn();
const addToWishlistSpy = vi.fn();
const deleteFromLibrarySpy = vi.fn();
const deleteFromWishlistSpy = vi.fn();

vi.mock('../../src/api/ApiService', () => ({
    apiService: {
        getBookByID: vi.fn(),
        updateRating: vi.fn(),
        updateStatus: vi.fn(),
    },
}));

vi.mock('../../src/composables/useBookActions', () => ({
    useBookActions: () => ({
        onAddToLibrary: addToLibrarySpy,
        onAddToWishlist: addToWishlistSpy,
        onDeleteFromLibrary: deleteFromLibrarySpy,
        onDeleteFromWishlist: deleteFromWishlistSpy,
    }),
}));

vi.mock('../../src/wrapper/AuthInfoWrapper.ts', () => {
    const authRef = ref(true);
    return {
        isAuthenticated: authRef,
        _testAuthRef: authRef,
    };
});

import { apiService } from '../../src/api/ApiService';
import BookDetail from '../../src/components/Book.vue';
import { _testAuthRef as authRef } from '../../src/wrapper/AuthInfoWrapper.ts';

const mockBook = {
    bookID: '123',
    title: 'Test Book',
    subtitle: 'Subtitle',
    description: 'A very good book',
    authors: ['Author One', 'Author Two'],
    publishDate: '2020',
    isbns: ['1234567890'],
    averageRating: 4.3,
    individualRating: 3,
    readingStatus: ReadingStatus.UNREAD,
    bookIsInLibrary: true,
    bookIsOnWishlist: false,
    coverURLLarge: 'https://example.com/cover.jpg',
};

const createWrapper = async (overrides = {}) => {
    const router = createRouter({
        history: createMemoryHistory(),
        routes: [{ path: '/book/:id', component: BookDetail }],
    });

    router.push('/book/123');
    await router.isReady();

    (apiService.getBookByID as any).mockResolvedValue({ ...mockBook, ...overrides });

    const wrapper = mount(BookDetail, {
        global: {
            plugins: [router],
        },
    });

    await flushPromises();
    return wrapper;
};

const triggerStatusEditFlow = async (shouldFail = false) => {
    if (shouldFail) {
        (apiService.updateStatus as any).mockRejectedValueOnce(new Error('Status update failed'));
    } else {
        (apiService.updateStatus as any).mockResolvedValueOnce();
    }

    const wrapper = await createWrapper();
    const editButtons = wrapper.findAll('button');
    const editStatusButton = [...editButtons].reverse().find(btn => btn.text().includes('Edit'));
    await editStatusButton?.trigger('click');
    await wrapper.find('select').setValue(ReadingStatus.READ);
    const saveButton = wrapper.findAll('button').find(btn => btn.text().includes('Save'));
    await saveButton?.trigger('click');
    await flushPromises();
    return wrapper;
};

describe('BookDetail.vue', () => {
    it('loads and displays book details', async () => {
        const wrapper = await createWrapper();
        expect(wrapper.html()).toContain('Test Book - Subtitle');
        expect(wrapper.html()).toContain('Author One, Author Two');
        expect(wrapper.html()).toContain('2020');
        expect(wrapper.html()).toContain('1234567890');
        expect(wrapper.html()).toContain('4.3');
    });

    it('handles missing cover gracefully', async () => {
        const wrapper = await createWrapper({ coverURLLarge: null });
        expect(wrapper.text()).toContain('No Cover found');
    });

    it('shows login message when not authenticated', async () => {
        authRef.value = false;
        const wrapper = await createWrapper();
        expect(wrapper.text()).toContain('You are not logged in');
    });

    it('enters edit mode for rating and cancels it', async () => {
        authRef.value = true;
        const wrapper = await createWrapper();
        const editButtons = wrapper.findAll('button');
        const editRatingButton = editButtons.find(btn => btn.text().includes('Edit'));
        await editRatingButton?.trigger('click');
        expect(wrapper.find('select').exists()).toBe(true);
        await wrapper.find('button.btn-outline-secondary').trigger('click');
        expect(wrapper.find('select').exists()).toBe(false);
    });

    it('saves edited rating', async () => {
        authRef.value = true;
        (apiService.updateRating as any).mockResolvedValueOnce();
        const wrapper = await createWrapper();
        const editButtons = wrapper.findAll('button');
        const editRatingButton = editButtons.find(btn => btn.text().includes('Edit'));
        await editRatingButton?.trigger('click');
        await wrapper.find('select').setValue('5');
        const saveButton = wrapper.findAll('button').find(btn => btn.text().includes('Save'));
        await saveButton?.trigger('click');
        expect(apiService.updateRating).toHaveBeenCalledWith('123', '5');
    });

    it('saves edited status', async () => {
        authRef.value = true;
        const wrapper = await triggerStatusEditFlow();
        expect(apiService.updateStatus).toHaveBeenCalledWith('123', ReadingStatus.READ);
    });

    it('shows error when status update fails', async () => {
        authRef.value = true;
        const wrapper = await triggerStatusEditFlow(true);
        expect(wrapper.text()).toContain('Status update failed');
    });

    it('adds book to library when not in library', async () => {
        authRef.value = true;
        addToLibrarySpy.mockClear();

        const wrapper = await createWrapper({
            bookIsInLibrary: false,
            bookIsOnWishlist: false,
        });

        const addBtn = wrapper.findAll('button').find(btn =>
            btn.text().includes('Add to library')
        );

        expect(addBtn).toBeTruthy();
        await addBtn!.trigger('click');
        expect(addToLibrarySpy).toHaveBeenCalledWith('123');
    });

    it('shows error when rating update fails', async () => {
        authRef.value = true;
        (apiService.updateRating as any).mockRejectedValueOnce(new Error('Rating update failed'));

        const wrapper = await createWrapper();
        const editRatingButton = wrapper.findAll('button').find(btn => btn.text().includes('Edit'));
        await editRatingButton?.trigger('click');

        await wrapper.find('select').setValue('5');
        const saveButton = wrapper.findAll('button').find(btn => btn.text().includes('Save'));
        await saveButton?.trigger('click');

        await flushPromises();

        expect(wrapper.text()).toContain('Rating update failed');
    });

    it('cancels editing status', async () => {
        authRef.value = true;
        const wrapper = await createWrapper();

        // Enter status edit mode
        const editButtons = wrapper.findAll('button');
        const editStatusButton = [...editButtons].reverse().find(btn => btn.text().includes('Edit'));
        await editStatusButton?.trigger('click');

        // Confirm we're in edit mode
        expect(wrapper.find('select').exists()).toBe(true);

        // Find and click Cancel
        const cancelButton = wrapper.findAll('button').find(btn => btn.text().includes('Cancel'));
        await cancelButton?.trigger('click');

        // Confirm edit mode exited
        expect(wrapper.find('select').exists()).toBe(false);
    });

});
