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


    <BaseBookList
        title=""
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
      <template #actions="{ book }">
        <div class="d-grid gap-2">
          <router-link class="btn btn-primary" :to="`/book/${book.bookID}`">Details</router-link>
          <button @click="onAddToLibrary(book.bookID)" v-if="isAuthenticated.value && !book.bookIsInLibrary" class="btn btn-primary">Add to Library</button>
          <button @click="onAddToWishlist(book.bookID)" v-if="isAuthenticated.value && !book.bookIsInLibrary && !book.bookIsOnWishlist" class="btn btn-primary">Add to Wishlist</button>
        </div>
      </template>

    </BaseBookList>
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

import BaseBookList from "./BaseBookList.vue";
import {defineComponent, onMounted, ref, watch} from 'vue';
import { apiService } from '../api/ApiService';
import {useBookList} from "../composables/useBookList.ts";
import {onBeforeRouteLeave} from "vue-router";
import {isAuthenticated} from "../wrapper/AuthInfoWrapper.ts";

export default defineComponent({
  name: 'BookListComponent',
  computed: {
    isAuthenticated() {
      return isAuthenticated
    }
  },
  components: {BaseBookList },

  setup() {

    // keywords from the last click on the search button
    let keywords : string = "";

    const keywordsFieldValue = ref("")

    /**
     * Perform the search when the user clicks on the search button
     */
    const onSearchClick = () =>{
      keywords = keywordsFieldValue.value;
      sessionStorage.setItem("searchKeywords", keywords)
      bookList.loadBooks(keywords);
    };

    const bookList = useBookList((start, size, keywords) => apiService.getKeywordSearch(keywords,start, size));
    const columns = [
      { label: 'Cover', field: 'coverURLSmall', slot: 'cover' },
      { label: 'Title', field: 'title', slot: 'title' },
      { label: 'Authors', field: 'authors', slot: 'authors' },
      { label: 'Release Date', field: 'publishDate' },
      { label: 'Average Rating', field: 'averageRating', slot: 'averageRating' },
      { label: 'Actions', field: 'actions', slot: 'actions' },
    ];

    onMounted(() => {
      const saved = sessionStorage.getItem('searchPage');
      const searchKeyWords = sessionStorage.getItem("searchKeywords");

      if (saved && searchKeyWords) {
        try {
          keywords = searchKeyWords;
          keywordsFieldValue.value = searchKeyWords;
          const { page, size } = JSON.parse(saved);
          bookList.setPagination(page || 1, size || bookList.pageSize.value);
          bookList.loadBooks(keywords);
        } catch (e) {
          console.warn('Invalid pagination data in sessionStorage');
        }
      }else{
        bookList.emptyInitBooks();
      }

    });

    const onAddToLibrary = async (bookID : string) =>{
      bookList.error.value = null
      try{
        await apiService.addBookToLibrary(bookID as string);
        await bookList.loadBooks(keywords)
      } catch (e: any){
        bookList.error.value = e.message || 'Failed to add book to library';
      }
    }

    const onAddToWishlist = async (bookID : string) =>{
      bookList.error.value = null
      try{
        await apiService.addBookToWishlist(bookID as string);
        await bookList.loadBooks(keywords)
      } catch (e: any){
        bookList.error.value = e.message || 'Failed to add book to wishlist';
      }
    }

    watch(() => [bookList.currentPage.value, bookList.pageSize.value], ([page, size]) => {
      sessionStorage.setItem('searchPage', JSON.stringify({ page, size }));
    });

    onBeforeRouteLeave((to) => {
      const isLeavingToBooks = to.name === 'Book';
      if (!isLeavingToBooks) {
        sessionStorage.removeItem('searchPage');
        sessionStorage.removeItem('searchKeywords')
      }
    });

    return {
      bookList,
      columns,
      keywordsFieldValue,
      onSearchClick,
      onAddToLibrary,
      onAddToWishlist,
    };
  },
});


</script>
