<script lang="ts" setup>
import { onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import { apiService } from '../api/ApiService';
import type { BookDTO } from '../dto/BookDTO';

const route = useRoute();
const bookID = route.params.id as string;

const book = ref<BookDTO | null>(null);
const loading = ref(false);
const error = ref<string | null>(null);

onMounted(async () => {
  console.log(bookID)

  if(bookID != null)
  {
    try {
      loading.value = true;
      book.value = await apiService.getBookByID(bookID);
    } catch (e: any) {
      error.value = e.message || 'Failed to load book';
    } finally {
      loading.value = false;
    }
  }

});
</script>

<template>
  <div>
    <div v-if="!bookID" class="text-danger">Error: You are accessing this page without a book ID. Please access this page from <router-link to="/">All Books</router-link>, <router-link to="/search">Search</router-link>, <router-link to="/library">your Library</router-link> or <router-link to="/wishlist">your wishlist</router-link>!</div>
    <div v-if="loading">Loading...</div>
    <div v-if="error" class="text-danger">{{ error }}</div>
    <div v-if="book">
      <h3>{{ book.subtitle ? (book.title + " - " + book.subtitle) : book.title }}</h3>
      <div class="container">
        <div class="row">
          <div class="col-4">
            <div v-if="book.coverURLLarge"><img :src="book.coverURLLarge" alt="Cover" /></div>
            <div v-else>No Cover found</div>
          </div>

          <div class="col-5">

            <p v-if="book.description">{{book.description}}</p>
            <p v-if="book.authors"><strong>Authors:</strong> {{ book.authors.join(', ') }}</p>
            <p v-else><strong>Authors:</strong> No author found</p>
            <p><strong>Published:</strong> {{ book.publishDate }}</p>
            <p><strong>Average Rating:</strong> {{ book.averageRating || 'Not rated yet' }}</p>
          </div>

          <div class="col-3 border border-secondary">
            <h4>Personal</h4>
            <p>You are not logged in. Please <router-link to="/login">login</router-link> to your account to manage this book. </p>
          </div>
        </div>
      </div>
    </div>
  </div>

</template>