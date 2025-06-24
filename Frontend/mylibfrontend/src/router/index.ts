import { createRouter, createWebHistory } from 'vue-router';

import Book from '../components/Book.vue';
import Library from '../components/Library.vue';
import Login from '../components/Login.vue';
import Search from '../components/Search.vue';
import Wishlist from '../components/Wishlist.vue';
import AllBooks from '../components/AllBooks.vue';

const routes = [
    // Homepage: shows all available books
    { path: '/', name: 'AllBooks', component: AllBooks },

    // Login page
    { path: '/login', name: 'Login', component: Login },

    // Signup mode handled via dynamic :signup param (e.g., /login/signup)
    { path: '/login/:signup', name: 'Signum', component: Login },

    // Book detail view by ID
    { path: '/book/:id', name: 'Book', component: Book },

    // Fallback for direct /book access without ID (shows error inside component)
    { path: '/book', name: 'BookWithoutID', component: Book },

    // Authenticated user’s personal library
    { path: '/library', name: 'Library', component: Library },

    // OpenLibrary keyword search results
    { path: '/search', name: 'Search', component: Search },

    // Authenticated user’s wishlist
    { path: '/wishlist', name: 'Wishlist', component: Wishlist },
];

const router = createRouter({
    history: createWebHistory(),
    routes,
});

export default router;