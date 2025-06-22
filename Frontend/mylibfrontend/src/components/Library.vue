<template>
  <BaseBookList
      title="Your Library"
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

    <template #individualRating="{ book }">
      {{book.individualRating ? `${book.individualRating} / 5` : 'Not rated yet'}}
    </template>
    <template #averageRating="{ book }">
      {{ book.averageRating ? `${book.averageRating} / 5` : 'Not rated yet' }}
    </template>

    <template #actions="{ book }">
      <router-link class="btn btn-primary" :to="`/book/${book.bookID}`">Details</router-link>
    </template>
  </BaseBookList>
</template>

<script lang="ts">
import {defineComponent, onBeforeMount, onMounted} from 'vue';
import { useBookList } from '../composables/useBookList';
import { apiService } from '../api/ApiService';
import BaseBookList from './BaseBookList.vue';
import {useRouter} from "vue-router";

export default defineComponent({
  name: 'LibraryBooks',
  components: { BaseBookList },
  setup() {
    const bookList = useBookList((start, size) => apiService.getLibrary(start, size));
    const columns = [
      {label: 'Cover', field: 'coverURLSmall', slot: 'cover'},
      {label: 'Title', field: 'title', slot: 'title'},
      {label: 'Authors', field: 'authors', slot: 'authors'},
      {label: 'Release Date', field: 'publishDate'},
      {label: "Reading Status", field: 'readingStatus'},
      {label: 'Your Rating', field: 'individualRating', slot: 'individualRating'},
      {label: 'Average Rating', field: 'averageRating', slot: 'averageRating'},
      {label: 'Actions', field: 'actions', slot: 'actions'}
    ];

    const router = useRouter()
    onBeforeMount(() => {
      if (!localStorage.getItem("is_authenticated")) {
        router.push("/login")
      }
    });

    onMounted(bookList.loadBooks);

    return {
      bookList,
      columns,
    };
  },
});
</script>