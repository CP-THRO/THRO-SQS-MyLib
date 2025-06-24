// Keeps authentication state in sync across components without full page reloads.

import { ref } from 'vue';

/**
 * Reactive flag indicating if the user is currently authenticated.
 * Initialized from localStorage.
 */
export const isAuthenticated = ref(localStorage.getItem('is_authenticated') !== null);

/**
 * Reactive reference to the logged-in user's username.
 * Initialized from localStorage.
 */
export const loggedInUsername = ref(localStorage.getItem('username'));

/**
 * Syncs reactive auth state with the latest values from localStorage.
 * Should be called after login, logout, or token changes.
 */
export const syncAuthState = () => {
    isAuthenticated.value = localStorage.getItem('is_authenticated') !== null;
    loggedInUsername.value = localStorage.getItem('username');
};