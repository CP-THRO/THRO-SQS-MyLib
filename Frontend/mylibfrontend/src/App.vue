<script setup lang="ts">

</script>

<template>

  <div>
      <nav class="navbar navbar-expand-lg bg-body-tertiary">
        <div class="container-fluid">
          <router-link class="navbar-brand" to="/">MyLib</router-link>
          <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarSupportedContent">
            <span class="navbar-toggler-icon"></span>
          </button>
          <div class="collapse navbar-collapse" id="navbarSupportedContent">
            <ul class="navbar-nav me-auto mb-2 mb-lg-0">
              <li class="nav-item">
                <router-link class="btn" to="/">Home</router-link>
              </li>
              <li class="nav-item">
                <router-link class="btn" to="/search">Search</router-link>
              </li>
              <li class="nav-item">
                <router-link class="btn" to="/library">Library</router-link>
              </li>
              <li class="nav-item">
                <router-link class="btn" to="/wishlist">Wishlist</router-link>
              </li>
            </ul>
            <ul class="navbar-nav">
              <li class="nav-item dropdown me-auto ">
                <button class="nav-link dropdown-toggle" href="#" data-bs-toggle="dropdown">
                  Account
                </button>
                <ul class="dropdown-menu dropdown-menu-end">
                  <li><router-link class="dropdown-item" to="/login">Login</router-link></li>
                  <li><router-link class="dropdown-item" to="/login">Sign Up</router-link></li>
                </ul>
              </li>
            </ul>
          </div>
        </div>

      </nav>
    <router-view class="container py-4 px-3 mx-auto" />
  </div>
</template>

<style scoped>

</style>
