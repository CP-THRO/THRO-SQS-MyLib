<script setup lang="ts">

</script>

<template>

  <div>
      <nav class="navbar navbar-expand-lg bg-body-tertiary">
        <div class="container-fluid">
          <a class="navbar-brand" href="#">MyLib</a>
          <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarSupportedContent">
            <span class="navbar-toggler-icon"></span>
          </button>
          <div class="collapse navbar-collapse" id="navbarSupportedContent">
            <ul class="navbar-nav me-auto mb-2 mb-lg-0">
              <li class="nav-item">
                <a class="nav-link active" href="#">Home</a>
              </li>
              <li class="nav-item">
                <a class="nav-link" href="#">Search</a>
              </li>
              <li class="nav-item">
                <a class="nav-link" href="#">Library</a>
              </li>
              <li class="nav-item">
                <a class="nav-link" href="#">Wishlist</a>
              </li>
            </ul>
            <ul class="navbar-nav">
              <li class="nav-item dropdown me-auto ">
                <button class="nav-link dropdown-toggle" href="#" data-bs-toggle="dropdown">
                  Account
                </button>
                <ul class="dropdown-menu dropdown-menu-end">
                  <li><a class="dropdown-item" href="#">Login</a></li>
                  <li><a class="dropdown-item" href="#">Sign Up</a></li>
                </ul>
              </li>
            </ul>
          </div>
        </div>

      </nav>
  </div>
</template>

<style scoped>

</style>
