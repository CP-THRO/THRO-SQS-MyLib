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
      <div class="d-grid gap-2">
        <router-link class="btn btn-primary" :to="`/book/${book.bookID}`">Details</router-link>
        <button @click="onDeleteFromLibrary(book.bookID)" type="button" class="btn btn-danger">Delete from Library</button>
      </div>
    </template>
  </BaseBookList>
</template>

<script lang="ts">
import {defineComponent, onBeforeMount, onMounted, watch} from 'vue';
import { useBookList } from '../composables/useBookList';
import { apiService } from '../api/ApiService';
import BaseBookList from './BaseBookList.vue';
import {onBeforeRouteLeave, useRouter} from "vue-router";

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

    const onDeleteFromLibrary = async (bookID : string) =>{
      bookList.error.value = null
      try{
        await apiService.deleteBookFromLibrary(bookID as string);
        await bookList.loadBooks()
      } catch (e: any){
        bookList.error.value = e.message || 'Failed to delete book from library';
      }
    }

    onMounted(() => {
      const saved = sessionStorage.getItem('libraryPage');

      if (saved) {
        try {
          const { page, size } = JSON.parse(saved);
          bookList.setPagination(page || 1, size || bookList.pageSize.value);
        } catch (e) {
          console.warn('Invalid pagination data in sessionStorage');
        }
      }
      bookList.loadBooks();
    });

    watch(() => [bookList.currentPage.value, bookList.pageSize.value], ([page, size]) => {
      sessionStorage.setItem('libraryPage', JSON.stringify({ page, size }));
    });

    onBeforeRouteLeave((to) => {
      const isLeavingToBooks = to.name === 'Book';
      if (!isLeavingToBooks) {
        sessionStorage.removeItem('libraryPage');
      }
    });


    return {
      bookList,
      columns,
      onDeleteFromLibrary,
    };
  },
});
</script>