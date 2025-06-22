
<template>
  <div>
    <table class="table">
      <thead>
      <tr>
        <th v-for="col in columns" :key="col.field">{{ col.label }}</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="book in books" :key="book.bookID">
        <td v-for="col in columns" :key="col.field">
          <slot v-if="col.slot" :name="col.slot" :book="book" />
          <span v-else>{{ book[col.field as keyof BookDTO] }}</span>
        </td>
      </tr>
      </tbody>
    </table>

    <div class="d-flex justify-content-between align-items-center mt-3">
      <div>
        <label for="pageSize" class="me-2">Results per page:</label>
        <select id="pageSize" class="form-select d-inline-block w-auto"
                :value="pageSize"
                @change="onPageSizeChange">
          <option v-for="size in pageSizes" :key="size" :value="size">{{ size }}</option>
        </select>
      </div>
      <div>
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
import { defineComponent } from 'vue';
import type { PropType} from "vue";

import type { BookDTO } from '../dto/BookDTO';

export default defineComponent({
  name: 'BookTable',
  props: {
    books: { type: Array as PropType<BookDTO[]>, required: true },
    columns: { type: Array as PropType<Array<{ label: string; field: string; slot?: string }>>, required: true },
    currentPage: { type: Number, required: true },
    totalPages: { type: Number, required: true },
    pageSize: { type: Number, required: true },
    pageSizes: { type: Array as PropType<number[]>, required: true }
  },
  emits: ['next-page', 'prev-page', 'page-size-change'],

  methods: {
    onPageSizeChange(event: Event) {
      const value = +(event.target as HTMLSelectElement).value;
      this.$emit('page-size-change', value);
    }
  }
});
</script>

<style scoped>

</style>