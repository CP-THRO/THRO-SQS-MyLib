<script setup lang="ts">
import { onMounted, onBeforeMount, ref } from 'vue';
import {watch} from "vue";
import {useRoute } from 'vue-router';
import {useRouter} from "vue-router";
import { apiService } from '../api/ApiService';
import {syncAuthState} from "../wrapper/AuthInfoWrapper.ts";

let route = useRoute();
let signup : string = route.params.signup as string;

const username = ref<string>("");
const password = ref<string>("");
const error = ref<string | null>(null);

let loginInProgress : boolean = false;

const router = useRouter()
watch(
    () => router.currentRoute.value,
    currentRoute=> {
      signup = currentRoute.params.signup as string;
    },
    { immediate: true }
)

// Track previous route (if coming from a protected page)
onBeforeMount(() => {
  if(localStorage.getItem("is_authenticated")){
    router.push("/")
  }

  const fromPath = router.options.history.state.back;
  if (route.path === '/login' && fromPath && typeof fromPath === 'string') {
    sessionStorage.setItem('loginRedirect', fromPath);
  }
});

const buttonAction = async () => {
  error.value = null;
  loginInProgress = true;

  if(signup){

    try{
      let responseCode = await apiService.signUp(username.value, password.value);
      if(responseCode === 409){
        error.value = "Error: Username already exists!";
      }else{ // automatic login after signup
        signup = ""
        await buttonAction()
      }

    }catch (e: any) {
      error.value = e.message || 'An error occurred';
    } finally {
      loginInProgress = false;
    }

  }else{

    try {

      let responseCode = await apiService.authenticate(username.value, password.value);
      if(responseCode === 403){
        error.value = "Error: Username or password incorrect"
      }else{
        // Redirect to previous path or fallback to home
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
  <div v-if="error" class="text-danger">{{error}}</div>
  <form>
    <div v-if="!loginInProgress" class="mb-3">
      <label for="usernameInput" class="form-label">Username</label>
      <input v-model="username" type="text" class="form-control" id="usernameInput">
    </div>
    <div class="mb-3">
      <label for="passwordInput" class="form-label">Password</label>
      <input v-model="password" type="password" class="form-control" id="passwordInput">
    </div>

    <button @click="buttonAction" type="button" class="btn btn-primary me-3">{{signup ? "Sign Up" : "Login"}}</button>
    <router-link :to="`${signup ? '/login' : '/login/signup'}`" class="btn btn-outline-secondary">{{signup ? "Login" : "Sign Up"}}</router-link>

  </form>


</div>
</template>

<style scoped>

</style>