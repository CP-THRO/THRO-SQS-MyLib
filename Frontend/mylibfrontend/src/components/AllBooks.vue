<template>
  <div>
    <!-- Heading for the book list section -->
    <h3>Books in libraries and wishlists</h3>

    <!-- Display loading state -->
    <div v-if="loading">Loading...</div>

    <!-- Display error message if any -->
    <div v-if="error" class="text-danger">{{ error }}</div>

    <!-- Show if no books were returned -->
    <div v-if="books?.numResults === 0">No books found.</div>

    <!-- Render dynamic book table if data is available -->
    <BookTable
        v-if="books"
        :books="books.books"
        :columns="columns"
        :currentPage="currentPage"
        :totalPages="totalPages"
        :pageSize="pageSize"
        :pageSizes="pageSizes"
        @next-page="nextPage"
        @prev-page="prevPage"
        @page-size-change="updatePageSize"
    >
      <!-- Slot for rendering the cover image -->
      <template #cover="{ book }">
        <img :src="book.coverURLSmall" alt="Cover" style="width: 50px;" />
      </template>

      <!-- Slot for rendering title and subtitle -->
      <template #title="{ book }">
        {{ book.subtitle ? `${book.title} - ${book.subtitle}` : book.title }}
      </template>

      <!-- Slot for rendering authors list -->
      <template #authors="{ book }">
        {{ book.authors.join(", ") }}
      </template>

      <!-- Slot for rendering average rating -->
      <template #averageRating="{ book }">
        {{ book.averageRating ? `${book.averageRating} / 5` : 'Not rated yet' }}
      </template>

      <!-- Slot for action buttons like 'Details' -->
      <template #actions="{ book }">
        <router-link class="btn btn-primary" :to="`/book/${book.bookID}`">Details</router-link>
      </template>
    </BookTable>
  </div>
</template>

<script lang="ts">
/**
 * BookListComponent
 * -----------------
 * A Vue component responsible for:
 * - Fetching paginated book data from the API
 * - Managing pagination and page size selection
 * - Displaying the BookTable component with dynamic slot-based columns
 */

import { defineComponent, ref, computed, onMounted, onActivated, watch } from 'vue';
import { apiService } from '../api/ApiService';
import type { BookListDTO } from '../dto/BookListDTO';
import BookTable from './BookTable.vue';

export default defineComponent({
  name: 'BookListComponent',
  components: { BookTable },

  setup() {
    // State variables for fetched data and UI status
    const books = ref<BookListDTO | null>(null);
    const loading = ref(false);
    const error = ref<string | null>(null);

    // Pagination state
    const pageSizes = [5, 10, 25, 50];
    const pageSize = ref(10);
    const currentPage = ref(1);

    // Calculate total pages based on total results and page size
    const totalPages = computed(() => {
      if (!books.value) return 1;
      return Math.ceil(books.value.numResults / pageSize.value);
    });

    /**
     * Fetch books from the API using the current page and size.
     */
    const loadBooks = async () => {
      loading.value = true;
      error.value = null;
      const startIndex = (currentPage.value - 1) * pageSize.value;
      try {
        books.value = await apiService.getAllBooks(startIndex, pageSize.value);
      } catch (e: any) {
        error.value = e.message || 'An error occurred';
      } finally {
        loading.value = false;
      }
    };

    /**
     * Go to the next page and reload data.
     */
    const nextPage = () => {
      if (currentPage.value < totalPages.value) {
        currentPage.value++;
        loadBooks();
      }
    };

    /**
     * Go to the previous page and reload data.
     */
    const prevPage = () => {
      if (currentPage.value > 1) {
        currentPage.value--;
        loadBooks();
      }
    };

    /**
     * Update the number of results per page and reload from page 1.
     * @param size - New page size selected by user
     */
    const updatePageSize = (size: number) => {
      pageSize.value = size;
      currentPage.value = 1;
      loadBooks();
    };

    /**
     * Column configuration for the dynamic table.
     * Each column may specify a named slot for custom rendering.
     */
    const columns = [
      { label: 'Cover', field: 'coverURLSmall', slot: 'cover' },
      { label: 'Title', field: 'title', slot: 'title' },
      { label: 'Authors', field: 'authors', slot: 'authors' },
      { label: 'Release Date', field: 'publishDate' },
      { label: 'Average Rating', field: 'averageRating', slot: 'averageRating' },
      { label: 'Actions', field: 'actions', slot: 'actions' }
    ];

    // Fetch data when component is first mounted or activated via <keep-alive>
    onMounted(loadBooks);
    onActivated(loadBooks);

    // Reload data if page size changes
    watch(pageSize, () => {
      currentPage.value = 1;
      loadBooks();
    });

    // Return state and functions to the template
    return {
      books,
      loading,
      error,
      columns,
      currentPage,
      pageSize,
      pageSizes,
      totalPages,
      nextPage,
      prevPage,
      updatePageSize
    };
  }
});
</script>

<style scoped>

</style>