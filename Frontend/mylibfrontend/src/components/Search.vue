<template>
  <div>
    <!-- Heading for the openlibrary search section -->
    <h3>OpenLibrary Search</h3>


    <!-- Form for the keyword search -->
    <div>
      <form class="row g-2">
        <div class="col-auto">
          <label for="inputKeywords" class="visually-hidden">Keywords</label>
          <input v-model="keywordsFieldValue" type="text" class="form-control" id="inputKeywords" placeholder="Keywords">
        </div>
        <div class="col-auto">
          <button @click="onSearchClick" type="button" class="btn btn-primary mb-3">Search</button>
        </div>
      </form>
    </div>


    <!-- Display loading state -->
    <div v-if="loading">Loading...</div>

    <!-- Display error message if any -->
    <div v-if="error" class="text-danger">{{ error }}</div>

    <!-- Show if no books were returned -->
    <div v-if="!loading && books?.numResults === 0 && searchHappened">No books found.</div>

    <div v-if="books && books?.skippedBooks > 0 && searchHappened">Due to inconsistencies in OpenLibrary, {{books?.skippedBooks}} book {{books?.skippedBooks ===1 ? "has" : "have"}} been omitted</div>

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
          {{ book.authors ? book.authors.join(", ") : "No Author found" }}
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

import { defineComponent, ref, computed, watch } from 'vue';
import { apiService } from '../api/ApiService';
import type { BookListDTO } from '../dto/BookListDTO';
import BookTable from './BookTable.vue';

export default defineComponent({
  name: 'BookListComponent',
  components: { BookTable },

  setup() {
    // State variables for fetched data and UI status
    const books = ref<BookListDTO>({numResults: 0, startIndex: 0, books: [], skippedBooks: 0,});
    const loading = ref(false);
    const error = ref<string | null>(null);
    const searchHappened = ref(false); //Flag to tell if there was already a search, to control the "No books found" message.

    // Pagination state
    const pageSizes = [5, 10, 25, 50];
    const pageSize = ref(10);
    const currentPage = ref(1);


    // keywords from the last click on the search button
    let keywords : string = "";

    const keywordsFieldValue = ref("")

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
        books.value = {numResults: 0, startIndex: 0, books: [], skippedBooks: 0,}; // reset before loading
        books.value = await apiService.getKeywordSearch(keywords, startIndex, pageSize.value);
      } catch (e: any) {
        error.value = e.message || 'An error occurred';
      } finally {
        loading.value = false;
        searchHappened.value = true;
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
     * Perform the search when the user clicks on the search button
     */
    const onSearchClick = () =>{
      keywords = keywordsFieldValue.value;
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
      { label: 'Actions', field: 'actions', slot: 'actions' }
    ];

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
      searchHappened,
      columns,
      currentPage,
      pageSize,
      pageSizes,
      totalPages,
      keywordsFieldValue,
      nextPage,
      prevPage,
      updatePageSize,
      onSearchClick,
    };

  }
});
</script>

<style scoped>

</style>