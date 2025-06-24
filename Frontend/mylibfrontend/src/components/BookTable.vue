<template>
  <div>
    <!-- Book data table -->
    <table class="table">
      <thead>
      <tr>
        <!-- Render each column header based on the dynamic config -->
        <th v-for="col in columns" :key="col.field">{{ col.label }}</th>
      </tr>
      </thead>

      <tbody>
      <!-- Render each row for a book -->
      <tr v-for="book in books" :key="book.bookID">
        <!-- Render each cell based on column definition -->
        <td v-for="col in columns" :key="col.field">
          <!-- If a slot is defined for this column, render it -->
          <slot v-if="col.slot" :name="col.slot" :book="book" />

          <!-- Otherwise, render the raw field value -->
          <span v-else>{{ book[col.field as keyof BookDTO] }}</span>
        </td>
      </tr>
      </tbody>
    </table>

    <!-- Pagination and page size controls -->
    <div class="d-flex justify-content-between align-items-center mt-3">
      <!-- Page size selector -->
      <div>
        <label for="pageSize" class="me-2">Results per page:</label>
        <select
            id="pageSize"
            class="form-select d-inline-block w-auto"
            :value="pageSize"
            @change="onPageSizeChange"
        >
          <option v-for="size in pageSizes" :key="size" :value="size">
            {{ size }}
          </option>
        </select>
      </div>

      <!-- Pagination buttons -->
      <div>
        <button
            class="btn btn-primary me-2"
            :disabled="currentPage <= 1"
            @click="$emit('prev-page')"
        >
          Previous
        </button>

        <span>Page {{ currentPage }} of {{ totalPages }}</span>

        <button
            class="btn btn-primary ms-2"
            :disabled="currentPage >= totalPages"
            @click="$emit('next-page')"
        >
          Next
        </button>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
/**
 * BookTable Component
 * -------------------
 * Reusable table for displaying a list of books with dynamic columns and layout.
 *
 * Features:
 * - Configurable columns with optional custom slots per field
 * - Pagination controls (current page, total pages)
 * - Page size selector with event-based updates
 */

import { defineComponent } from 'vue';
import type { PropType } from 'vue';
import type { BookDTO } from '../dto/BookDTO';

export default defineComponent({
  name: 'BookTable',

  props: {
    /**
     * Array of book objects to render.
     */
    books: {
      type: Array as PropType<BookDTO[]>,
      required: true,
    },

    /**
     * Column configuration.
     * Each entry includes a field name, display label, and optional slot name.
     */
    columns: {
      type: Array as PropType<Array<{ label: string; field: string; slot?: string }>>,
      required: true,
    },

    /**
     * Current page index (1-based).
     */
    currentPage: {
      type: Number,
      required: true,
    },

    /**
     * Total number of pages available.
     */
    totalPages: {
      type: Number,
      required: true,
    },

    /**
     * Current number of items per page.
     */
    pageSize: {
      type: Number,
      required: true,
    },

    /**
     * Allowed options for items per page.
     */
    pageSizes: {
      type: Array as PropType<number[]>,
      required: true,
    },
  },

  emits: [
    'next-page',         // Triggered when clicking the "Next" button
    'prev-page',         // Triggered when clicking the "Previous" button
    'page-size-change',  // Triggered when a new page size is selected
  ],

  methods: {
    /**
     * Emit page size selection changes to the parent.
     */
    onPageSizeChange(event: Event) {
      const value = +(event.target as HTMLSelectElement).value;
      this.$emit('page-size-change', value);
    },
  },
});
</script>
