import { createRouter, createWebHistory } from 'vue-router'
import Book from '../components/Book.vue'
import Library from "../components/Library.vue";
import Login from "../components/Login.vue";
import Search from "../components/Search.vue";
import Wishlist from "../components/Wishlist.vue";
import AllBooks from "../components/AllBooks.vue";

const routes = [
    { path: '/', component: AllBooks },
    { path: '/login', component: Login },
    { path: '/book', component: Book },
    { path: '/library', component: Library},
    { path: '/search', component: Search},
    { path: '/wishlist', component: Wishlist}
]

const router = createRouter({
    history: createWebHistory(),
    routes,
})

export default router