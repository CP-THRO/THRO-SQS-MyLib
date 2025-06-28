<script lang="ts" setup>
import { onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import { ApiService } from '../api/ApiService';
import type { BookDTO } from '../dto/BookDTO';
import { isAuthenticated } from "../wrapper/AuthInfoWrapper.ts";
import { ReadingStatus } from "../dto/ReadingStatus.ts";
import { useBookActions } from '../composables/useBookActions';

// Get the book ID from the route
const route = useRoute();
const bookID = route.params.id as string;

// Reactive state for book data and UI states
const book = ref<BookDTO | null>(null);
const loading = ref(false);
const error = ref<string | null>(null);
const errorPersonalSection = ref<string | null>(null);

// Rating and reading status selections
const ratingSelection = ref<number>(5);
type ReadingStatusType = typeof ReadingStatus[keyof typeof ReadingStatus];
const statusSelection = ref<ReadingStatusType>(ReadingStatus.UNREAD);

// Toggle edit modes
const editRating = ref(false);
const editStatus = ref(false);

/**
 * Loads book details from the API and syncs selection state.
 */
const loadBook = async () => {
  if (bookID != null) {
    try {
      loading.value = true;
      book.value = await ApiService.getInstance().getBookByID(bookID);

      if (book.value.individualRating !== 0) {
        ratingSelection.value = book.value.individualRating;
        statusSelection.value = book.value.readingStatus as ReadingStatusType;
      }
    } catch (e: any) {
      error.value = e.message || 'Failed to load book';
    } finally {
      loading.value = false;
    }
  }
};

// Use composable actions with automatic error handling and reload
const {
  onAddToLibrary,
  onAddToWishlist,
  onDeleteFromLibrary,
  onDeleteFromWishlist
} = useBookActions(
    { error: errorPersonalSection },
    loadBook
);

// Enable editing of the rating
const onEditRating = () => {
  editRating.value = true;
};

// Save the edited rating
const onEditRatingSave = async () => {
  try {
    await ApiService.getInstance().updateRating(book.value?.bookID as string, ratingSelection.value);
    await loadBook();
    editRating.value = false;
  } catch (e: any) {
    errorPersonalSection.value = e.message || 'Failed to update rating';
  }
};

// Abort editing the rating
const onEditRatingCancel = () => {
  editRating.value = false;
};

// Enable reading status editing
const onEditStatus = () => {
  editStatus.value = true;
};

// Save edited reading status
const onEditStatusSave = async () => {
  try {
    await ApiService.getInstance().updateStatus(book.value?.bookID as string, statusSelection.value);
    await loadBook();
    editStatus.value = false;
  } catch (e: any) {
    errorPersonalSection.value = e.message || 'Failed to update status';
  }
};

// Abort editing reading status
const onEditStatusCancel = () => {
  editStatus.value = false;
};

onMounted(loadBook);

</script>

<template>
  <div>
    <!-- Show warning if page accessed without a valid book ID -->
    <div v-if="!bookID" class="text-danger">
      Error: You are accessing this page without a book ID. Please access this page from
      <router-link to="/">All Books</router-link>,
      <router-link to="/search">Search</router-link>,
      <router-link to="/library">your Library</router-link>, or
      <router-link to="/wishlist">your Wishlist</router-link>!
    </div>

    <!-- Show loading and top-level errors -->
    <div v-if="loading">Loading...</div>
    <div v-if="error" class="text-danger">{{ error }}</div>

    <!-- Book details -->
    <div v-if="book">
      <h3>{{ book.subtitle ? `${book.title} - ${book.subtitle}` : book.title }}</h3>

      <div class="container">
        <div class="row">
          <!-- Cover image -->
          <div class="col-4">
            <div v-if="book.coverURLLarge">
              <img :src="book.coverURLLarge" alt="Cover" />
            </div>
            <div v-else>No Cover found</div>
          </div>

          <!-- Book metadata -->
          <div class="col-5">
            <p v-if="book.description">{{ book.description }}</p>
            <p>
              <strong>Authors:</strong>
              {{ book.authors?.length ? book.authors.join(', ') : 'No author found' }}
            </p>
            <p><strong>Published:</strong> {{ book.publishDate }}</p>
            <p v-if="book.isbns?.length"><strong>ISBN:</strong> {{ book.isbns.join(', ') }}</p>
            <p><strong>Average Rating:</strong> {{ book.averageRating || 'Not rated yet' }}</p>
          </div>

          <!-- Personal controls (auth-only) -->
          <div class="col-3 border border-secondary">
            <h4>Personal</h4>

            <!-- Not logged in -->
            <div v-if="!isAuthenticated">
              You are not logged in. Please <router-link to="/login">login</router-link> to manage this book.
            </div>

            <!-- Logged in controls -->
            <div v-else>
              <div v-if="errorPersonalSection" class="text-danger">{{ errorPersonalSection }}</div>

              <!-- Library/Wishlist buttons -->
              <div class="d-grid gap-2">
                <button @click="onDeleteFromLibrary(book.bookID)" v-if="book.bookIsInLibrary" class="btn btn-danger">Delete from library</button>

                <button @click="onAddToLibrary(book.bookID)" v-else class="btn btn-primary">Add to library</button>

                <button @click="onAddToWishlist(book.bookID)" v-if="!book.bookIsInLibrary && !book.bookIsOnWishlist" class="btn btn-primary">Add to wishlist</button>

                <button @click="onDeleteFromWishlist(bookID)" v-else-if="book.bookIsOnWishlist" class="btn btn-danger">Delete from wishlist</button>
              </div>

              <!-- Rating and status controls -->
              <div v-if="book.bookIsInLibrary" class="mt-2">
                <!-- Rating -->
                <p>
                  <strong>Your Rating:</strong>
                  <span v-if="!editRating">
                    {{ book.individualRating ? `${book.individualRating} / 5` : 'Not rated' }}
                    <button @click="onEditRating" class="btn btn-primary ms-2">Edit</button>
                  </span>
                  <span v-else>
                    <select v-model="ratingSelection" class="form-select d-inline-block w-auto">
                      <option>1</option>
                      <option>2</option>
                      <option>3</option>
                      <option>4</option>
                      <option>5</option>
                    </select>
                    / 5
                    <button @click="onEditRatingSave" class="btn btn-primary ms-2">Save</button>
                    <button @click="onEditRatingCancel" class="btn btn-outline-secondary ms-2">Cancel</button>
                  </span>
                </p>

                <!-- Reading status -->
                <p>
                  <strong>Your Status:</strong>
                  <span v-if="!editStatus">
                    {{ book.readingStatus }}
                    <button @click="onEditStatus" class="btn btn-primary ms-2">Edit</button>
                  </span>
                  <span v-else>
                    <select v-model="statusSelection" class="form-select d-inline-block w-auto">
                      <option
                          v-for="(value, key) in ReadingStatus"
                          :key="key"
                          :value="value"
                      >
                        {{ key }}
                      </option>
                    </select>
                    <button @click="onEditStatusSave" class="btn btn-primary ms-2">Save</button>
                    <button @click="onEditStatusCancel" class="btn btn-outline-secondary ms-2">Cancel</button>
                  </span>
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>