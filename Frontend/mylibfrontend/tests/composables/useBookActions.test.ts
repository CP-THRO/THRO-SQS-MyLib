import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { ref } from 'vue';
import { useBookActions } from '../../src/composables/useBookActions.ts';
import { apiService } from '../../src/api/ApiService.ts';

vi.mock('../../api/ApiService');

describe('useBookActions', () => {
    let errorRef = ref<string | null>(null);
    let target = { error: errorRef };
    let reload: any;
    let keywords: any;

    beforeEach(() => {
        errorRef.value = null;
        reload = vi.fn().mockResolvedValue(undefined);
        keywords = vi.fn().mockReturnValue('harry potter');
    });

    afterEach(() => {
        vi.clearAllMocks();
    });

    it('calls onAddToLibrary and reloads with keywords', async () => {
        const add = vi.spyOn(apiService, 'addBookToLibrary').mockResolvedValue();

        const { onAddToLibrary } = useBookActions(target, reload, keywords);
        await onAddToLibrary('OL1');

        expect(add).toHaveBeenCalledWith('OL1');
        expect(reload).toHaveBeenCalledWith('harry potter');
        expect(errorRef.value).toBeNull();
    });

    it('calls onAddToWishlist and reloads without keywords', async () => {
        const add = vi.spyOn(apiService, 'addBookToWishlist').mockResolvedValue();

        const { onAddToWishlist } = useBookActions(target, reload);
        await onAddToWishlist('OL2');

        expect(add).toHaveBeenCalledWith('OL2');
        expect(reload).toHaveBeenCalledWith(undefined);
        expect(errorRef.value).toBeNull();
    });

    it('calls onDeleteFromLibrary and sets error on failure', async () => {
        const err = new Error('fail lib');
        vi.spyOn(apiService, 'deleteBookFromLibrary').mockRejectedValue(err);

        const { onDeleteFromLibrary } = useBookActions(target, reload, keywords);
        await onDeleteFromLibrary('OL3');

        expect(errorRef.value).toBe('fail lib');
    });

    it('calls onDeleteFromWishlist and uses fallback message if no error message', async () => {
        const err = {};
        vi.spyOn(apiService, 'deleteBookFromWishlist').mockRejectedValue(err);

        const { onDeleteFromWishlist } = useBookActions(target, reload, keywords);
        await onDeleteFromWishlist('OL4');

        expect(errorRef.value).toBe('An error occurred');
    });
});
