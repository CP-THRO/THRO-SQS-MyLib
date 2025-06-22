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
        <!-- Render each cell dynamically -->
        <td v-for="col in columns" :key="col.field">
          <!-- If a slot is defined for this column, use it -->
          <slot v-if="col.slot" :name="col.slot" :book="book" />

          <!-- Otherwise, show the raw field value -->
          <span v-else>{{ book[col.field as keyof BookDTO] }}</span>
        </td>
      </tr>
      </tbody>
    </table>

    <!-- Pagination and page size controls -->
    <div class="d-flex justify-content-between align-items-center mt-3">
      <div>
        <label for="pageSize" class="me-2">Results per page:</label>

        <!-- Page size selector -->
        <select id="pageSize"
                class="form-select d-inline-block w-auto"
                :value="pageSize"
                @change="onPageSizeChange">
          <option v-for="size in pageSizes" :key="size" :value="size">
            {{ size }}
          </option>
        </select>
      </div>

      <div>
        <!-- Pagination controls -->
        <button class="btn btn-primary me-2"
                :disabled="currentPage <= 1"
                @click="$emit('prev-page')">
          Previous
        </button>

        <span>Page {{ currentPage }} of {{ totalPages }}</span>

        <button class="btn btn-primary ms-2"
                :disabled="currentPage >= totalPages"
                @click="$emit('next-page')">
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
 * A reusable, dynamic table component for displaying book data.
 * Features:
 * - Dynamic columns via configuration
 * - Custom content slots for flexible rendering (images, buttons, etc.)
 * - Pagination and page size selection
 */

import { defineComponent } from 'vue';
import type { PropType } from 'vue';
import type { BookDTO } from '../dto/BookDTO';

export default defineComponent({
  name: 'BookTable',

  props: {
    /**
     * List of books to display in the table.
     */
    books: {
      type: Array as PropType<BookDTO[]>,
      required: true
    },

    /**
     * Columns config: defines how headers and fields render.
     * Each entry may include a named slot for custom rendering.
     */
    columns: {
      type: Array as PropType<Array<{ label: string; field: string; slot?: string }>>,
      required: true
    },

    /**
     * Current page number (used to update pagination controls).
     */
    currentPage: {
      type: Number,
      required: true
    },

    /**
     * Total number of pages (used to disable/enable navigation).
     */
    totalPages: {
      type: Number,
      required: true
    },

    /**
     * Current number of results per page.
     */
    pageSize: {
      type: Number,
      required: true
    },

    /**
     * List of page size options shown in the selector.
     */
    pageSizes: {
      type: Array as PropType<number[]>,
      required: true
    }
  },

  emits: [
    'next-page',         // Triggered when the user clicks the 'Next' button
    'prev-page',         // Triggered when the user clicks the 'Previous' button
    'page-size-change'   // Triggered when the user changes page size
  ],

  methods: {
    /**
     * Handle changes to the page size dropdown.
     * Emits a 'page-size-change' event with the selected size.
     */
    onPageSizeChange(event: Event) {
      const value = +(event.target as HTMLSelectElement).value;
      this.$emit('page-size-change', value);
    }
  }
});
</script>

<style scoped>
</style>
