//Helper for syncing auth information between ApiService and Navbar without reloading the page

import { ref } from 'vue'

export const isAuthenticated = ref(localStorage.getItem('is_authenticated') !== null)
export const loggedInUsername = ref(localStorage.getItem('username'))

export const syncAuthState = () => {
    isAuthenticated.value = localStorage.getItem('is_authenticated') !== null
    loggedInUsername.value = localStorage.getItem('username')
}