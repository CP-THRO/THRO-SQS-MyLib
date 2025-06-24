<template>
  <BaseBookList
      title="Books in libraries and wishlists"
      :books="bookList.books.value"
      :loading="bookList.loading.value"
      :error="bookList.error.value as string"
      :columns="columns"
      :currentPage="bookList.currentPage.value"
      :totalPages="bookList.totalPages.value"
      :pageSize="bookList.pageSize.value"
      :pageSizes="bookList.pageSizes"
      @next-page="bookList.nextPage"
      @prev-page="bookList.prevPage"
      @page-size-change="bookList.updatePageSize"
  >
    <template #averageRating="{ book }">
      {{ book.averageRating ? `${book.averageRating} / 5` : 'Not rated yet' }}
    </template>

    <template #actions="{ book }">
      <div class="d-grid gap-2">
        <router-link class="btn btn-primary" :to="`/book/${book.bookID}`">Details</router-link>
        <button @click="onAddToLibrary(book.bookID)" v-if="isAuthenticated.value && !book.bookIsInLibrary" class="btn btn-primary">Add to Library</button>
        <button @click="onAddToWishlist(book.bookID)" v-if="isAuthenticated.value && !book.bookIsInLibrary && !book.bookIsOnWishlist" class="btn btn-primary">Add to Wishlist</button>
      </div>
    </template>
  </BaseBookList>
</template>

<script lang="ts">
import {defineComponent, onMounted, watch} from 'vue';
import { useBookList } from '../composables/useBookList';
import { apiService } from '../api/ApiService';
import BaseBookList from './BaseBookList.vue';
import {isAuthenticated} from "../wrapper/AuthInfoWrapper.ts";
import { onBeforeRouteLeave } from 'vue-router';


export default defineComponent({
  name: 'AllBooks',
  computed: {
    isAuthenticated() {
      return isAuthenticated
    }
  },
  components: { BaseBookList },
  setup() {
    const bookList = useBookList((start, size) => apiService.getAllBooks(start, size));
    const columns = [
      { label: 'Cover', field: 'coverURLSmall', slot: 'cover' },
      { label: 'Title', field: 'title', slot: 'title' },
      { label: 'Authors', field: 'authors', slot: 'authors' },
      { label: 'Release Date', field: 'publishDate' },
      { label: 'Average Rating', field: 'averageRating', slot: 'averageRating' },
      { label: 'Actions', field: 'actions', slot: 'actions' },
    ];

    onMounted(() => {
      const saved = sessionStorage.getItem('allBooksPage');

      if (saved) {
        try {
          const { page, size } = JSON.parse(saved);
          bookList.setPagination(page || 1, size || bookList.pageSize.value);
        } catch (e) {
          console.warn('Invalid pagination data in sessionStorage');
        }
      }
      bookList.loadBooks();
    });

    const onAddToLibrary = async (bookID : string) =>{
      bookList.error.value = null
      try{
        await apiService.addBookToLibrary(bookID as string);
        await bookList.loadBooks()
      } catch (e: any){
        bookList.error.value = e.message || 'Failed to add book to library';
      }
    }

    const onAddToWishlist = async (bookID : string) =>{
      bookList.error.value = null
      try{
        await apiService.addBookToWishlist(bookID as string);
        await bookList.loadBooks()
      } catch (e: any){
        bookList.error.value = e.message || 'Failed to add book to wishlist';
      }
    }

    watch(() => [bookList.currentPage.value, bookList.pageSize.value], ([page, size]) => {
      sessionStorage.setItem('allBooksPage', JSON.stringify({ page, size }));
    });

    onBeforeRouteLeave((to) => {
      const isLeavingToBooks = to.name === 'Book';
      if (!isLeavingToBooks) {
        sessionStorage.removeItem('allBooksPage');
      }
    });

    return {
      bookList,
      columns,
      onAddToLibrary,
      onAddToWishlist,
    };
  },
});
</script>