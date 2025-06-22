<template>
  <div>
    <h3>{{ title }}</h3>

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
        @next-page="$emit('next-page')"
        @prev-page="$emit('prev-page')"
        @page-size-change="$emit('page-size-change', $event)"
    >

      <template #cover="{ book }">
        <img :src="book.coverURLSmall" alt="Cover" style="width: 50px;" />
      </template>

      <template #title="{ book }">
        {{ book.subtitle ? `${book.title} - ${book.subtitle}` : book.title }}
      </template>

      <template #authors="{ book }">
        {{ book.authors ? (book.authors.join(', ')) : ("No author found") }}
      </template>
      <!-- Dynamically forward all named slots -->
      <template v-for="(_, name) in $slots" #[name]="slotProps" :key="name">
        <slot :name="name" v-bind="slotProps" />
      </template>
    </BookTable>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import type { PropType } from "vue";
import BookTable from './BookTable.vue';
import type { BookListDTO } from '../dto/BookListDTO';

export default defineComponent({
  name: 'BaseBookList',
  components: { BookTable },
  props: {
    title: { type: String, required: true },
    books: Object as PropType<BookListDTO | null>,
    loading: { type: Boolean, required: true },
    error: { type: String, default: null },
    columns: { type: Array as PropType<any[]>, required: true },
    currentPage: { type: Number, required: true },
    totalPages: { type: Number, required: true },
    pageSize: { type: Number, required: true },
    pageSizes: { type: Array as PropType<number[]>, required: true },
  },
  emits: ['next-page', 'prev-page', 'page-size-change'],
});
</script>