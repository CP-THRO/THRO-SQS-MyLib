<template>
  <div>
    <h3>OpenLibrary Search</h3>

    <form class="row g-2">
      <div class="col-auto">
        <label for="inputKeywords" class="visually-hidden">Keywords</label>
        <input
            v-model="keywordsFieldValue"
            type="text"
            class="form-control"
            id="inputKeywords"
            placeholder="Keywords"
        />
      </div>
      <div class="col-auto">
        <button @click="onSearchClick" type="button" class="btn btn-primary mb-3">Search</button>
      </div>
    </form>

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
          <button @click="onAddToLibrary(book.bookID)" v-if="isAuthenticated.value && !book.bookIsInLibrary" class="btn btn-primary"> Add to Library </button>
          <button @click="onAddToWishlist(book.bookID)" v-if="isAuthenticated.value && !book.bookIsInLibrary && !book.bookIsOnWishlist" class="btn btn-primary">Add to Wishlist</button>
        </div>
      </template>
    </BaseBookList>
  </div>
</template>

<script lang="ts">
import {defineComponent, ref, computed} from 'vue';
import {useBookList} from '../composables/useBookList';
import {usePaginationState} from '../composables/usePaginationState';
import {useBookActions} from '../composables/useBookActions';
import {apiService} from '../api/ApiService';
import {isAuthenticated} from '../wrapper/AuthInfoWrapper';
import BaseBookList from './BaseBookList.vue';

export default defineComponent({
  name: 'BookListComponent',
  components: {BaseBookList},
  setup() {
    const keywordsFieldValue = ref('');
    let keywords = '';

    const bookList = useBookList((start, size, keywords) =>
        apiService.getKeywordSearch(keywords, start, size)
    );

    const onSearchClick = () => {
      keywords = keywordsFieldValue.value;
      sessionStorage.setItem('searchKeywords', keywords);
      bookList.loadBooks(keywords);
    };

    usePaginationState(
        bookList,
        'searchPage',
        () => bookList.loadBooks(keywords),
        () => {
          const savedKeywords = sessionStorage.getItem('searchKeywords');
          if (savedKeywords) {
            keywords = savedKeywords;
            keywordsFieldValue.value = savedKeywords;
          }
        },
        ['searchPage', 'searchKeywords']
    );

    const {onAddToLibrary, onAddToWishlist} = useBookActions(
        bookList,
        bookList.loadBooks,
        () => keywords
    );

    const columns = [
      {label: 'Cover', field: 'coverURLSmall', slot: 'cover'},
      {label: 'Title', field: 'title', slot: 'title'},
      {label: 'Authors', field: 'authors', slot: 'authors'},
      {label: 'Release Date', field: 'publishDate'},
      {label: 'Average Rating', field: 'averageRating', slot: 'averageRating'},
      {label: 'Actions', field: 'actions', slot: 'actions'},
    ];

    return {
      bookList,
      keywordsFieldValue,
      onSearchClick,
      onAddToLibrary,
      onAddToWishlist,
      columns,
      isAuthenticated: computed(() => isAuthenticated),
    };
  },
});
</script>
