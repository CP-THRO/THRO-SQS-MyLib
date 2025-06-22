<template>
  <div>
    <h3>Books in libraries and wishlists</h3>
    <div v-if="loading">Loading...</div>
    <div v-if="error" class="text-danger">{{ error }}</div>
    <div v-if="books?.numResults === 0">No books found.</div>

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
      <template #cover="{ book }">
        <img :src="book.coverURLSmall" alt="Cover" style="width: 50px;" />
      </template>

      <template #title="{ book }">
        {{ book.subtitle ? `${book.title} - ${book.subtitle}` : book.title }}
      </template>

      <template #authors="{book}">
        {{book.authors.join(", ")}}
      </template>


      <template #averageRating="{ book }">
        {{book.averageRating ? `${book.averageRating} / 5` : 'Not rated yet'}}
      </template>

      <template #actions="{ book }">
        <button class="btn btn-outline-secondary">Details</button>
      </template>
    </BookTable>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, computed, onMounted, onActivated, watch } from 'vue';
import { apiService } from '../api/ApiService';
import type { BookListDTO } from '../dto/BookListDTO';
import BookTable from './BookTable.vue';

export default defineComponent({
  name: 'BookListComponent',
  components: { BookTable },

  setup() {
    const books = ref<BookListDTO | null>(null);
    const loading = ref(false);
    const error = ref<string | null>(null);

    const pageSizes = [5, 10, 25, 50];
    const pageSize = ref(10);
    const currentPage = ref(1);

    const totalPages = computed(() => {
      if (!books.value) return 1;
      return Math.ceil(books.value.numResults / pageSize.value);
    });

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

    const nextPage = () => {
      if (currentPage.value < totalPages.value) {
        currentPage.value++;
        loadBooks();
      }
    };

    const prevPage = () => {
      if (currentPage.value > 1) {
        currentPage.value--;
        loadBooks();
      }
    };

    const updatePageSize = (size: number) => {
      pageSize.value = size;
      currentPage.value = 1;
      loadBooks();
    };

    const columns = [
      { label: 'Cover', field: 'coverURLSmall', slot: 'cover' },
      { label: 'Title', field: 'title', slot: 'title' },
      { label: 'Authors', field: 'authors', slot: 'authors' },
      { label: 'Release Date', field: 'publishDate' },
      { label: 'Average Rating', field: 'averageRating', slot: 'averageRating' },
      { label: 'Actions', field: 'actions', slot: 'actions' }
    ];

    onMounted(loadBooks);
    onActivated(loadBooks);

    watch(pageSize, () => {
      currentPage.value = 1;
      loadBooks();
    });

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