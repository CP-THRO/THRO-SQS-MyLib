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

vi.mock('../../src/api/ApiService', () => {
    //define mocks INSIDE the factory
    const signUpMock = vi.fn().mockResolvedValue(200);
    const authenticateMock = vi.fn().mockResolvedValue(200);

    // attach them so tests can import and manipulate them
    return {
        ApiService: {
            getInstance: vi.fn().mockReturnValue({
                signUp: signUpMock,
                authenticate: authenticateMock,
            }),
        },
        __mocks: {
            signUpMock,
            authenticateMock,
        },
        handleApiError: vi.fn(),
        attachAuthToken: vi.fn(),
    };
});

vi.mock('../../src/wrapper/AuthInfoWrapper.ts', () => ({
    syncAuthState: vi.fn(),
}));

import Login from '../../src/components/Login.vue';

//Import mocks from the mock module
import {
    __mocks as apiServiceMocks,
} from '../../src/api/ApiService';
import { syncAuthState } from '../../src/wrapper/AuthInfoWrapper';

describe('Login.vue', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        localStorage.clear();

        currentRoute.value = {
            fullPath: '/login',
            path: '/login',
            params: { signup: '' },
        };

        pushMock.mockClear();

        apiServiceMocks.signUpMock.mockReset().mockResolvedValue(200);
        apiServiceMocks.authenticateMock.mockReset().mockResolvedValue(200);
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
            params: { signup: 'signup' },
        };

        apiServiceMocks.signUpMock.mockResolvedValueOnce(409);

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
            params: { signup: 'signup' },
        };

        apiServiceMocks.signUpMock.mockResolvedValueOnce(201);
        apiServiceMocks.authenticateMock.mockResolvedValueOnce(200);
        syncAuthState.mockClear();

        const wrapper = mount(Login);
        await flushPromises();

        await wrapper.find('#usernameInput').setValue('newuser');
        await wrapper.find('#passwordInput').setValue('newpass');
        await wrapper.find('button.btn-primary').trigger('click');
        await flushPromises();

        expect(apiServiceMocks.signUpMock).toHaveBeenCalledWith('newuser', 'newpass');
        expect(apiServiceMocks.authenticateMock).toHaveBeenCalledWith('newuser', 'newpass');
        expect(syncAuthState).toHaveBeenCalled();
        expect(pushMock).toHaveBeenCalled();
    });

    it('shows login error for incorrect credentials', async () => {
        apiServiceMocks.authenticateMock.mockResolvedValueOnce(403);

        const wrapper = mount(Login);
        await flushPromises();

        await wrapper.find('#usernameInput').setValue('wronguser');
        await wrapper.find('#passwordInput').setValue('wrongpass');
        await wrapper.find('button.btn-primary').trigger('click');
        await flushPromises();

        expect(wrapper.text()).toContain('Error: Username or password incorrect');
    });

    it('logs in successfully and syncs auth state', async () => {
        apiServiceMocks.authenticateMock.mockResolvedValueOnce(200);
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
        apiServiceMocks.authenticateMock.mockRejectedValueOnce(new Error('Network error'));

        const wrapper = mount(Login);
        await flushPromises();

        await wrapper.find('#usernameInput').setValue('user');
        await wrapper.find('#passwordInput').setValue('pass');
        await wrapper.find('button.btn-primary').trigger('click');
        await flushPromises();

        expect(wrapper.text()).toContain('Network error');
    });
});
