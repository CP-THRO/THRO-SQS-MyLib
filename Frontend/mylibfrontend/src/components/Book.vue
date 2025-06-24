<script lang="ts" setup>
import { onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import { apiService } from '../api/ApiService';
import type { BookDTO } from '../dto/BookDTO';
import {isAuthenticated} from "../wrapper/AuthInfoWrapper.ts";
import {ReadingStatus} from "../dto/ReadingStatus.ts";

const route = useRoute();
const bookID = route.params.id as string;

const book = ref<BookDTO | null>(null);
const loading = ref(false);
const error = ref<string | null>(null);
const errorPersonalSection = ref<string | null>(null);

const ratingSelection = ref<number>(5);
type ReadingStatusType = typeof ReadingStatus[keyof typeof ReadingStatus];
const statusSelection = ref<ReadingStatusType>(ReadingStatus.UNREAD as ReadingStatusType);

const editRating = ref<boolean>(false);

let editStatus = ref<boolean>(false);


const onAddToLibrary = async () =>{
  errorPersonalSection.value = null
  try{
    await apiService.addBookToLibrary(book.value?.bookID as string);
    await loadBook()
  } catch (e: any){
    errorPersonalSection.value = e.message || 'Failed to add book to library';
  }
}

const onAddToWishlist = async () =>{
  errorPersonalSection.value = null
  try{
    await apiService.addBookToWishlist(book.value?.bookID as string);
    await loadBook()
  } catch (e: any){
    errorPersonalSection.value = e.message || 'Failed to add book to wishlist';
  }
}

const onDeleteFromLibrary = async () =>{
  errorPersonalSection.value = null
  try{
    await apiService.deleteBookFromLibrary(book.value?.bookID as string);
    await loadBook()
  } catch (e: any){
    errorPersonalSection.value = e.message || 'Failed to delete book from library';
  }
}

const onDeleteFromWishlist = async () =>{
  errorPersonalSection.value = null
  try{
    await apiService.deleteBookFromWishlist(book.value?.bookID as string);
    await loadBook()
  } catch (e: any){
    errorPersonalSection.value = e.message || 'Failed to delete book from wishlist';
  }
}

const onEditRating = async () =>{
  editRating.value = true;
}

const onEditRatingSave = async () =>{
  try{
    await apiService.updateRating(book.value?.bookID as string, ratingSelection.value);
    await loadBook();
    editRating.value = false;
  } catch (e: any){
    errorPersonalSection.value = e.message || 'Failed to update rating';
  }
}

const onEditRatingCancel = async () =>{
  editRating.value = false;
}

const onEditStatus = async () =>{
  editStatus.value = true;
}

const onEditStatusSave = async () =>{
  try{
    await apiService.updateStatus(book.value?.bookID as string, statusSelection.value);
    await loadBook();
    editStatus.value = false;
  } catch (e: any){
    errorPersonalSection.value = e.message || 'Failed to update status';
  }
}

const onEditStatusCancel = async () =>{
  editStatus.value = false;
}

const loadBook = async () =>{
  if(bookID != null)
  {
    try {
      loading.value = true;
      book.value = await apiService.getBookByID(bookID);
      if(book.value.individualRating != 0){
        ratingSelection.value = book.value.individualRating;
        statusSelection.value = book.value.readingStatus as ReadingStatusType;
      }

    } catch (e: any) {
      error.value = e.message || 'Failed to load book';
    } finally {
      loading.value = false;
    }
  }
}



onMounted(loadBook);
</script>

<template>
  <div>
    <div v-if="!bookID" class="text-danger">Error: You are accessing this page without a book ID. Please access this page from <router-link to="/">All Books</router-link>, <router-link to="/search">Search</router-link>, <router-link to="/library">your Library</router-link> or <router-link to="/wishlist">your wishlist</router-link>!</div>
    <div v-if="loading">Loading...</div>
    <div v-if="error" class="text-danger">{{ error }}</div>
    <div v-if="book">
      <h3>{{ book.subtitle ? (book.title + " - " + book.subtitle) : book.title }}</h3>
      <div class="container">
        <div class="row">
          <div class="col-4">
            <div v-if="book.coverURLLarge"><img :src="book.coverURLLarge" alt="Cover" /></div>
            <div v-else>No Cover found</div>
          </div>

          <div class="col-5">

            <p v-if="book.description">{{book.description}}</p>
            <p v-if="book.authors"><strong>Authors:</strong> {{ book.authors.join(', ') }}</p>
            <p v-else><strong>Authors:</strong> No author found</p>
            <p><strong>Published:</strong> {{ book.publishDate }}</p>
            <p v-if="book.isbns?.length"><strong>ISBN:</strong> {{book.isbns.join(",")}}</p>
            <p><strong>Average Rating:</strong> {{ book.averageRating || 'Not rated yet' }}</p>
          </div>

          <div class="col-3 border border-secondary">
            <h4>Personal</h4>
            <div v-if="!isAuthenticated">You are not logged in. Please <router-link to="/login">login</router-link> to your account to manage this book. </div>
            <div v-else>

              <div v-if="errorPersonalSection" class="text-danger">{{errorPersonalSection}}</div>
              <div class="d-grid gap-2">

                <button @click="onDeleteFromLibrary" v-if="book.bookIsInLibrary" class="btn btn-danger" >Delete from library</button>
                <button @click="onAddToLibrary" v-else class="btn btn-primary">Add to library</button>
                <button @click="onAddToWishlist" v-if="!book.bookIsInLibrary && !book.bookIsOnWishlist" class="btn btn-primary">Add to wishlist</button>
                <button @click="onDeleteFromWishlist" v-else-if="book.bookIsOnWishlist"  class="btn btn-danger">Delete from wishlist</button>

              </div>

              <div v-if="book.bookIsInLibrary" class="mt-2">
                <p>
                  <strong>Your Rating: </strong>
                  <span v-if="!editRating">{{book.individualRating?`${book.individualRating} / 5` : "Not rated"}} <button @click="onEditRating" type="button" class="btn btn-primary ms-2">Edit</button></span>
                  <span v-else>
                      <select v-model="ratingSelection" class="form-select d-inline-block w-auto">
                          <option>1</option>
                          <option>2</option>
                          <option>3</option>
                          <option>4</option>
                          <option>5</option>
                      </select> / 5
                    <button @click="onEditRatingSave" type="button" class="btn btn-primary ms-2">Save</button>
                    <button @click="onEditRatingCancel" type="button" class="btn btn-outline-secondary ms-2">Cancel</button>

                  </span>
                </p>

                <p>
                  <strong>Your Status: </strong>
                  <span v-if="!editStatus">{{book.readingStatus}} <button @click="onEditStatus" type="button" class="btn btn-primary ms-2">Edit</button></span>
                  <span v-else>
                    <select v-model="statusSelection" class="form-select d-inline-block w-auto">
                      <option
                          v-for="(value, key) in ReadingStatus"
                          :key="key"
                          :value="value">
                          {{ key }}
                      </option>
                    </select>
                    <button @click="onEditStatusSave" type="button" class="btn btn-primary ms-2">Save</button>
                    <button @click="onEditStatusCancel" type="button" class="btn btn-outline-secondary ms-2">Cancel</button>
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