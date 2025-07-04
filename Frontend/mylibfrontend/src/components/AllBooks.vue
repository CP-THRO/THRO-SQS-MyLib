<template>
  <!-- Book list display using the shared BaseBookList component -->
  <BaseBookList
      title="Saved books"
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
    <!-- Custom slot for rendering the average rating -->
    <template #averageRating="{ book }">
      {{ book.averageRating ? `${book.averageRating} / 5` : 'Not rated yet' }}
    </template>

    <!-- Custom slot for action buttons: View, Add to Library/Wishlist -->
    <template #actions="{ book }">
      <div class="d-grid gap-2">
        <router-link class="btn btn-primary" :to="`/book/${book.bookID}`">Details</router-link>

        <!-- Show "Add to Library" if user is authenticated and book isn't already added -->
        <button
            @click="onAddToLibrary(book.bookID)"
            v-if="isAuthenticated.value && !book.bookIsInLibrary"
            class="btn btn-primary"
        >
          Add to Library
        </button>

        <!-- Show "Add to Wishlist" if not in library and not already wished for -->
        <button
            @click="onAddToWishlist(book.bookID)"
            v-if="isAuthenticated.value && !book.bookIsInLibrary && !book.bookIsOnWishlist"
            class="btn btn-primary"
        >
          Add to Wishlist
        </button>
      </div>
    </template>
  </BaseBookList>
</template>

<script lang="ts">
import {defineComponent, computed} from 'vue';
import BaseBookList from './BaseBookList.vue';
import { useBookList } from '../composables/useBookList';
import { usePaginationState } from '../composables/usePaginationState';
import { useBookActions } from '../composables/useBookActions';
import { ApiService } from '../api/ApiService';
import { isAuthenticated } from '../wrapper/AuthInfoWrapper';

export default defineComponent({
  name: 'AllBooks',
  components: { BaseBookList },
  setup() {

    // Load all books using the API service with pagination support
    const bookList = useBookList((start, size) => ApiService.getInstance().getAllBooks(start, size));

    // Composable for handling add-to-library/wishlist actions
    const { onAddToLibrary, onAddToWishlist } = useBookActions(bookList, bookList.loadBooks);

    // Restore pagination state on mount and handle cleanup on route leave
    usePaginationState(bookList, 'allBooksPage', bookList.loadBooks);

    // Column definitions for BaseBookList; includes slots for custom rendering
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
      onAddToWishlist,
      isAuthenticated: computed(() => isAuthenticated),
    };
  },
});
</script>