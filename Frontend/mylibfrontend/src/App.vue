<script setup lang="ts">
import { isAuthenticated, loggedInUsername, syncAuthState } from './wrapper/AuthInfoWrapper.ts';
import { onMounted } from 'vue';

/**
 * Ensures reactive auth state is synchronized when the component mounts.
 */
onMounted(() => {
  syncAuthState();
});

/**
 * Logs the user out by clearing relevant localStorage entries and syncing auth state.
 */
const logOut = () => {
  localStorage.removeItem('is_authenticated');
  localStorage.removeItem('username');
  localStorage.removeItem('auth_token');
  syncAuthState();
};
</script>

<template>
  <div>
    <nav class="navbar navbar-expand-lg bg-body-tertiary">
      <div class="container-fluid">
        <router-link class="navbar-brand" to="/">MyLib</router-link>

        <!-- Responsive collapse toggle -->
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarSupportedContent">
          <span class="navbar-toggler-icon"></span>
        </button>

        <!-- Main navigation links -->
        <div class="collapse navbar-collapse" id="navbarSupportedContent">
          <ul class="navbar-nav me-auto mb-2 mb-lg-0">
            <li class="nav-item"><router-link class="btn" to="/">Home</router-link></li>
            <li class="nav-item"><router-link class="btn" to="/search">Search</router-link></li>
            <li class="nav-item"><router-link class="btn" to="/library">Library</router-link></li>
            <li class="nav-item"><router-link class="btn" to="/wishlist">Wishlist</router-link></li>
          </ul>

          <!-- Account dropdown: shows login/signup or logout depending on auth status -->
          <ul class="navbar-nav">
            <li class="nav-item dropdown me-auto">
              <button class="nav-link dropdown-toggle" data-bs-toggle="dropdown">
                Account
              </button>
              <div>
                <ul class="dropdown-menu dropdown-menu-end">
                  <template v-if="!isAuthenticated">
                    <li><router-link class="dropdown-item" to="/login">Login</router-link></li>
                    <li><router-link class="dropdown-item" to="/login/signup">Sign Up</router-link></li>
                  </template>
                  <template v-else>
                    <li><button @click="logOut" class="dropdown-item">Logout</button></li>
                    <li><hr class="dropdown-divider" /></li>
                    <li><span class="dropdown-item">Logged in as: <strong>{{ loggedInUsername }}</strong></span></li>
                  </template>
                </ul>
              </div>
            </li>
          </ul>
        </div>
      </div>
    </nav>

    <!-- Route content injection point -->
    <router-view class="container py-4 px-3 mx-auto" />
  </div>
</template>