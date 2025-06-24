import { createRouter, createWebHistory } from 'vue-router'
import Book from '../components/Book.vue'
import Library from "../components/Library.vue";
import Login from "../components/Login.vue";
import Search from "../components/Search.vue";
import Wishlist from "../components/Wishlist.vue";
import AllBooks from "../components/AllBooks.vue";

const routes = [
    { path: '/', name:"AllBooks" , component: AllBooks },
    { path: '/login', name:"Login", component: Login },
    { path: '/login/:signup', name:"Signum", component: Login },
    { path: '/book/:id', name:"Book", component: Book },
    { path: '/book', name:"BookWithoutID", component: Book },
    { path: '/library', name:"Library", component: Library},
    { path: '/search', name:"Search", component: Search},
    { path: '/wishlist', name:"Wishlist", component: Wishlist}
]

const router = createRouter({
    history: createWebHistory(),
    routes,
})

export default router