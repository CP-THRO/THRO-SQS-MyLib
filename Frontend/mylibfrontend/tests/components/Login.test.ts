import { describe, it, expect, beforeEach, vi } from 'vitest';
import { mount, flushPromises } from '@vue/test-utils';
import { ref } from 'vue';

const currentRoute = ref({
    fullPath: '/login',
    path: '/login',
    params: { signup: '' },  // default login mode
});

const pushMock = vi.fn().mockResolvedValue();

vi.mock('vue-router', () => ({
    useRoute: () => currentRoute.value,
    useRouter: () => ({
        push: pushMock,
        options: { history: { state: { back: '/previous' } } },
        currentRoute,
    }),
}));

vi.mock('../../src/api/ApiService', () => ({
    apiService: {
        signUp: vi.fn().mockResolvedValue(200),
        authenticate: vi.fn().mockResolvedValue(200),
    },
}));

vi.mock('../../src/wrapper/AuthInfoWrapper.ts', () => ({
    syncAuthState: vi.fn(),
}));

import Login from '../../src/components/Login.vue';

describe('Login.vue', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        localStorage.clear();
        // Reset to login mode before each test
        currentRoute.value = {
            fullPath: '/login',
            path: '/login',
            params: { signup: '' },
        };
        pushMock.mockClear();
    });

    it('redirects immediately if already authenticated', async () => {
        localStorage.setItem('is_authenticated', 'true');
        mount(Login);
        await flushPromises();
        expect(pushMock).toHaveBeenCalledWith('/');
    });

    it('shows signup error if username exists', async () => {
        currentRoute.value = {
            fullPath: '/login/signup',
            path: '/login/signup',
            params: { signup: 'signup' }, // signup mode enabled
        };

        const { apiService } = await import('../../src/api/ApiService');
        apiService.signUp.mockResolvedValueOnce(409);

        const wrapper = mount(Login);
        await flushPromises();

        await wrapper.find('#usernameInput').setValue('user');
        await wrapper.find('#passwordInput').setValue('pass');
        await wrapper.find('button.btn-primary').trigger('click');
        await flushPromises();

        expect(wrapper.text()).toContain('Error: Username already exists!');
    });

    it('successfully signs up and then logs in', async () => {
        currentRoute.value = {
            fullPath: '/login/signup',
            path: '/login/signup',
            params: { signup: 'signup' }, // signup mode enabled
        };

        const { apiService } = await import('../../src/api/ApiService');
        apiService.signUp.mockResolvedValueOnce(201);
        apiService.authenticate.mockResolvedValueOnce(200);

        const { syncAuthState } = await import('../../src/wrapper/AuthInfoWrapper.ts');
        syncAuthState.mockClear();

        const wrapper = mount(Login);
        await flushPromises();

        await wrapper.find('#usernameInput').setValue('newuser');
        await wrapper.find('#passwordInput').setValue('newpass');
        await wrapper.find('button.btn-primary').trigger('click');
        await flushPromises();

        expect(apiService.signUp).toHaveBeenCalledWith('newuser', 'newpass');
        expect(apiService.authenticate).toHaveBeenCalledWith('newuser', 'newpass');
        expect(syncAuthState).toHaveBeenCalled();
        expect(pushMock).toHaveBeenCalled();
    });

    it('shows login error for incorrect credentials', async () => {
        const { apiService } = await import('../../src/api/ApiService');
        apiService.authenticate.mockResolvedValueOnce(403);

        const wrapper = mount(Login);
        await flushPromises();

        await wrapper.find('#usernameInput').setValue('wronguser');
        await wrapper.find('#passwordInput').setValue('wrongpass');
        await wrapper.find('button.btn-primary').trigger('click');
        await flushPromises();

        expect(wrapper.text()).toContain('Error: Username or password incorrect');
    });

    it('logs in successfully and syncs auth state', async () => {
        const { apiService } = await import('../../src/api/ApiService');
        apiService.authenticate.mockResolvedValueOnce(200);

        const { syncAuthState } = await import('../../src/wrapper/AuthInfoWrapper.ts');
        syncAuthState.mockClear();

        const wrapper = mount(Login);
        await flushPromises();

        await wrapper.find('#usernameInput').setValue('user');
        await wrapper.find('#passwordInput').setValue('pass');
        await wrapper.find('button.btn-primary').trigger('click');
        await flushPromises();

        expect(syncAuthState).toHaveBeenCalled();
        expect(pushMock).toHaveBeenCalled();
    });

    it('displays generic error on API failure', async () => {
        const { apiService } = await import('../../src/api/ApiService');
        apiService.authenticate.mockRejectedValueOnce(new Error('Network error'));

        const wrapper = mount(Login);
        await flushPromises();

        await wrapper.find('#usernameInput').setValue('user');
        await wrapper.find('#passwordInput').setValue('pass');
        await wrapper.find('button.btn-primary').trigger('click');
        await flushPromises();

        expect(wrapper.text()).toContain('Network error');
    });
});
