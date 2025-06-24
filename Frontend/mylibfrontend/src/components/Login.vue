<script setup lang="ts">
import { onBeforeMount, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { apiService } from '../api/ApiService';
import { syncAuthState } from "../wrapper/AuthInfoWrapper.ts";

// Route & navigation state
let route = useRoute();
let signup: string = route.params.signup as string;
const router = useRouter();

// Form fields
const username = ref<string>("");
const password = ref<string>("");
const error = ref<string | null>(null);

// UI state
let loginInProgress: boolean = false;

// Watch for route changes to update signup mode dynamically
watch(
    () => router.currentRoute.value,
    currentRoute => {
      signup = currentRoute.params.signup as string;
    },
    { immediate: true }
);

// If already logged in, redirect immediately
// Also store previous page for post-login redirection
onBeforeMount(() => {
  if (localStorage.getItem("is_authenticated")) {
    router.push("/");
  }

  const fromPath = router.options.history.state.back;
  if (route.path === '/login' && fromPath && typeof fromPath === 'string') {
    sessionStorage.setItem('loginRedirect', fromPath);
  }
});

/**
 * Main button handler for login or sign-up mode.
 */
const buttonAction = async () => {
  error.value = null;
  loginInProgress = true;

  if (signup) {
    // Handle user sign-up
    try {
      const responseCode = await apiService.signUp(username.value, password.value);
      if (responseCode === 409) {
        error.value = "Error: Username already exists!";
      } else {
        // Auto-login after successful sign-up
        signup = "";
        await buttonAction();
      }
    } catch (e: any) {
      error.value = e.message || 'An error occurred';
    } finally {
      loginInProgress = false;
    }

  } else {
    // Handle login
    try {
      const responseCode = await apiService.authenticate(username.value, password.value);
      if (responseCode === 403) {
        error.value = "Error: Username or password incorrect";
      } else {
        // Sync app-wide auth state and redirect
        syncAuthState();
        const redirectPath = sessionStorage.getItem('loginRedirect') || '/';
        sessionStorage.removeItem('loginRedirect');
        await router.push(redirectPath);
      }
    } catch (e: any) {
      error.value = e.message || 'An error occurred';
    } finally {
      loginInProgress = false;
    }
  }
};
</script>

<template>
  <div class="w-50">
    <!-- Error message -->
    <div v-if="error" class="text-danger">{{ error }}</div>

    <form v-if="!loginInProgress">
      <!-- Username input (disabled while login is in progress) -->
      <div  class="mb-3">
        <label for="usernameInput" class="form-label">Username</label>
        <input v-model="username" type="text" class="form-control" id="usernameInput" />
      </div>

      <!-- Password input -->
      <div class="mb-3">
        <label for="passwordInput" class="form-label">Password</label>
        <input v-model="password" type="password" class="form-control" id="passwordInput" />
      </div>

      <!-- Action buttons -->
      <button
          @click="buttonAction"
          type="button"
          class="btn btn-primary me-3"
      >
        {{ signup ? "Sign Up" : "Login" }}
      </button>

      <!-- Switch between login/signup routes -->
      <router-link
          :to="`${signup ? '/login' : '/login/signup'}`"
          class="btn btn-outline-secondary"
      >
        {{ signup ? "Login" : "Sign Up" }}
      </router-link>
    </form>
  </div>
</template>