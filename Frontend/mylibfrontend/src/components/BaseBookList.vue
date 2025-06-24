<template>
  <div>
    <!-- Section title -->
    <h3>{{ title }}</h3>

    <!-- Loading and error messages -->
    <div v-if="loading">Loading...</div>
    <div v-if="error" class="text-danger">{{ error }}</div>
    <div v-if="books?.numResults === 0">No books found.</div>

    <!-- Render BookTable only if book data is available -->
    <BookTable
        v-if="books"
        :books="books.books"
        :columns="columns"
        :currentPage="currentPage"
        :totalPages="totalPages"
        :pageSize="pageSize"
        :pageSizes="pageSizes"
        @next-page="$emit('next-page')"
        @prev-page="$emit('prev-page')"
        @page-size-change="$emit('page-size-change', $event)"
    >

      <!-- Slot: Book cover image -->
      <template #cover="{ book }">
        <img :src="book.coverURLSmall" alt="Cover" style="width: 50px;" />
      </template>

      <!-- Slot: Book title and optional subtitle -->
      <template #title="{ book }">
        {{ book.subtitle ? `${book.title} - ${book.subtitle}` : book.title }}
      </template>

      <!-- Slot: Book authors (comma-separated) -->
      <template #authors="{ book }">
        {{ book.authors ? (book.authors.join(', ')) : ("No author found") }}
      </template>

      <!-- Forward any additional named slots from the parent -->
      <template v-for="(_, name) in $slots" #[name]="slotProps" :key="name">
        <slot :name="name" v-bind="slotProps" />
      </template>
    </BookTable>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import type { PropType } from 'vue';
import BookTable from './BookTable.vue';
import type { BookListDTO } from '../dto/BookListDTO';

export default defineComponent({
  name: 'BaseBookList',
  components: { BookTable },
  props: {
    /** Section title */
    title: { type: String, required: true },

    /** Book data to display (can be null before fetch completes) */
    books: Object as PropType<BookListDTO | null>,

    /** Loading indicator flag */
    loading: { type: Boolean, required: true },

    /** Optional error message */
    error: { type: String, default: null },

    /** Column metadata for BookTable */
    columns: { type: Array as PropType<any[]>, required: true },

    /** Current page index */
    currentPage: { type: Number, required: true },

    /** Total number of available pages */
    totalPages: { type: Number, required: true },

    /** Currently selected page size */
    pageSize: { type: Number, required: true },

    /** List of selectable page sizes */
    pageSizes: { type: Array as PropType<number[]>, required: true },
  },
  emits: ['next-page', 'prev-page', 'page-size-change'],
});
</script>
