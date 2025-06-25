// src/tests/router/index.test.ts
import { describe, it, expect, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { defineComponent, nextTick } from 'vue';
import router from '../../router/index';

const DummyComponent = defineComponent({
    template: '<router-view />',
});

describe('Router', () => {
    let wrapper: ReturnType<typeof mount>;

    beforeEach(async () => {
        // Always reset the router to root
        router.push('/');
        wrapper = mount(DummyComponent, {
            global: { plugins: [router] },
        });
        await router.isReady();
    });

    it('navigates to book detail with ID', async () => {
        await router.push('/book/OL123');
        await nextTick();

        expect(router.currentRoute.value.name).toBe('Book');
        expect(router.currentRoute.value.params.id).toBe('OL123');
    });

    it('navigates to login with signup mode', async () => {
        await router.push('/login/signup');
        await nextTick();

        expect(router.currentRoute.value.name).toBe('Signum');
        expect(router.currentRoute.value.params.signup).toBe('signup');
    });

    it('navigates to Wishlist', async () => {
        await router.push('/wishlist');
        await nextTick();

        expect(router.currentRoute.value.name).toBe('Wishlist');
    });

    it('falls back to /book without ID', async () => {
        await router.push('/book');
        await nextTick();

        expect(router.currentRoute.value.name).toBe('BookWithoutID');
    });
});
