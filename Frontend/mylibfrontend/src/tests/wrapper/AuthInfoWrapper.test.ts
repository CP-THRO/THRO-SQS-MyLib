import { describe, it, expect, beforeEach } from 'vitest';
import { isAuthenticated, loggedInUsername, syncAuthState } from '../../wrapper/AuthInfoWrapper.ts';

describe('authState', () => {
    beforeEach(() => {
        localStorage.clear();
        isAuthenticated.value = false;
        loggedInUsername.value = null;
    });

    it('initializes isAuthenticated as false when localStorage has no key', () => {
        expect(isAuthenticated.value).toBe(false);
    });

    it('initializes loggedInUsername from localStorage', () => {
        localStorage.setItem('username', 'testuser');
        // Manually call sync to re-read from localStorage
        syncAuthState();
        expect(loggedInUsername.value).toBe('testuser');
    });

    it('sets isAuthenticated to true if localStorage has is_authenticated', () => {
        localStorage.setItem('is_authenticated', 'true');
        syncAuthState();
        expect(isAuthenticated.value).toBe(true);
    });

    it('sets loggedInUsername to null if username is missing', () => {
        localStorage.removeItem('username');
        syncAuthState();
        expect(loggedInUsername.value).toBe(null);
    });

    it('updates reactive values after login', () => {
        localStorage.setItem('is_authenticated', 'true');
        localStorage.setItem('username', 'alice');
        syncAuthState();
        expect(isAuthenticated.value).toBe(true);
        expect(loggedInUsername.value).toBe('alice');
    });

    it('updates reactive values after logout', () => {
        localStorage.removeItem('is_authenticated');
        localStorage.removeItem('username');
        syncAuthState();
        expect(isAuthenticated.value).toBe(false);
        expect(loggedInUsername.value).toBe(null);
    });
});