<template>
  <!-- Reusable paginated book list component for wishlist display -->
  <BaseBookList
      title="Your Wishlist"
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
    <!-- Slot: average rating display -->
    <template #averageRating="{ book }">
      {{ book.averageRating ? `${book.averageRating} / 5` : 'Not rated yet' }}
    </template>

    <!-- Slot: action buttons for each book -->
    <template #actions="{ book }">
      <div class="d-grid gap-2">
        <router-link class="btn btn-primary" :to="`/book/${book.bookID}`">Details</router-link>
        <button @click="onAddToLibrary(book.bookID)" class="btn btn-primary">Add to Library</button>
        <button @click="onDeleteFromWishlist(book.bookID)" class="btn btn-danger">Delete from Wishlist</button>
      </div>
    </template>
  </BaseBookList>
</template>

<script lang="ts">
import { defineComponent, computed, onBeforeMount } from 'vue';
import { useRouter } from 'vue-router';
import { useBookList } from '../composables/useBookList';
import { usePaginationState } from '../composables/usePaginationState';
import { useBookActions } from '../composables/useBookActions';
import { apiService } from '../api/ApiService';
import BaseBookList from './BaseBookList.vue';
import { isAuthenticated } from '../wrapper/AuthInfoWrapper';

export default defineComponent({
  name: 'WishlistBooks',
  components: { BaseBookList },

  setup() {
    const router = useRouter();

    // Initialize paginated book list for the user's wishlist
    const bookList = useBookList((start, size) => apiService.getWishlist(start, size));

    // Redirect to login if the user is not authenticated
    onBeforeMount(() => {
      if (!localStorage.getItem('is_authenticated')) {
        router.push('/login');
      }
    });

    // Persist and restore pagination state for wishlist
    usePaginationState(bookList, 'wishlistPage', bookList.loadBooks);

    // Action handlers for moving a book to the library or removing it from the wishlist
    const { onAddToLibrary, onDeleteFromWishlist } = useBookActions(bookList, bookList.loadBooks);

    // Column configuration for BaseBookList
    const columns = [
      { label: 'Cover', field: 'coverURLSmall', slot: 'cover' },
      { label: 'Title', field: 'title', slot: 'title' },
      { label: 'Authors', field: 'authors', slot: 'authors' },
      { label: 'Release Date', field: 'publishDate' },
      { label: 'Average Rating', field: 'averageRating', slot: 'averageRating' },
      { label: 'Actions', field: 'actions', slot: 'actions' },
    ];

    return {
      bookList,
      columns,
      onAddToLibrary,
      onDeleteFromWishlist,
      isAuthenticated: computed(() => isAuthenticated),
    };
  },
});
</script>