<script lang="ts">
import {defineComponent, onActivated, onMounted, ref, computed} from 'vue';
import { watch } from 'vue'; // Make sure this is imported at the top
import {apiService} from '../api/ApiService.ts';
import type {BookListDTO} from '../dto/BookListDTO.ts';

export default defineComponent({
  name: 'BookListComponent',

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
      const startIndex = (currentPage.value -1 ) * pageSize.value;
      try {
        books.value = await apiService.getAllBooks(startIndex, pageSize.value);
        console.log(books.value)
      } catch (e: any) {
        error.value = e.message || 'An error occurred';
      } finally {
        loading.value = false;
      }
    };

    // Fetch when component is first mounted
    onMounted(() => {
      loadBooks();
    });

    // Fetch again if using <keep-alive>
    onActivated(() => {
      loadBooks();
    });

    watch(pageSize, () => {
      currentPage.value = 1; // reset to first page
      loadBooks();           // fetch new results
    });

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

    return {
      pageSize,
      pageSizes,
      books,
      loading,
      error,
      loadBooks,
      currentPage,
      totalPages,
      nextPage,
      prevPage,
    };
  },
});
</script>


  <template>
    <div>
      <h3 class="Books">Books in libraries and wishlists</h3>
      <div v-if="loading">Loading...</div>
      <div v-if="error" class="error">{{ error }}</div>
      <div v-if="books?.numResults == 0">There are currently no books in any library or wishlist. Be the first!</div>

      <div  v-if="books && books?.books.length > 0">
        <table class="table">
            <thead>
                <tr>
                    <th scope="col">Cover</th>
                    <th scope="col">Title</th>
                    <th scope="col">Authors</th>
                    <th scope="col">Release Date</th>
                    <th scope="col">Average Rating</th>
                    <th scope="col"></th>
                </tr>
            </thead>

          <tbody>
          <tr v-for="book in books.books" :key="book.bookID">
            <td><img :src="book.coverURLSmall" alt="Cover" style="width: 50px; height: auto;" /></td>
            <td>{{ book.subtitle ? `${book.title} - ${book.subtitle}` : book.title }}</td>
            <td>{{ book.authors.join(", ")}}</td>
            <td>{{ book.publishDate }}</td>
            <td v-if="book.averageRating == 0">Not rated yet</td>
            <td v-else>{{`${book.averageRating} / 5`}}</td>
          </tr>
          </tbody>
        </table>

        <div>
          <label for="pageSize">Results per page: </label>
          <select class="form-select w-auto d-inline-block ms-2" id="pageSize" v-model.number="pageSize">
            <option v-for="size in pageSizes" :key="size" :value="size">{{ size }}</option>
          </select>
        </div>

        <div v-if="books && books.books.length > 0" style="margin-top: 1rem;">
          <button class="btn btn-primary me-2" @click="prevPage" :disabled="currentPage <= 1">Previous</button>
          <span>Page {{ currentPage }} of {{ totalPages }}</span>
          <button class="btn btn-primary ms-2" @click="nextPage" :disabled="currentPage >= totalPages">Next</button>
        </div>
      </div>
    </div>
  </template>

<style scoped>

</style>