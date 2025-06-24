import axios from 'axios'
import type {AxiosInstance} from 'axios';
import type {BookDTO} from "../dto/BookDTO.ts";
import type {BookListDTO} from "../dto/BookListDTO.ts";
import type {AuthRequestDTO} from "../dto/AuthRequestDTO.ts";
import type {AddBookRequestDTO} from "../dto/AddBookRequestDTO.ts";
import type {ChangeBookRatingDTO} from "../dto/ChangeBookRatingDTO.ts";
import type {ReadingStatusType} from "../dto/ReadingStatus.ts";
import type {ChangeBookReadingStatusRequestDTO} from "../dto/ChangeBookReadingStatus.dto.ts";

class ApiService {
    private static instance: ApiService;
    private readonly http: AxiosInstance;

    private constructor() {
        this.http = axios.create({
            baseURL: 'http://localhost:8080', // Placeholder for now
            headers: {
                'Content-Type': 'application/json',
            },
        });

        this.http.interceptors.response.use(
            (response) => response,
            (error) => {
                console.error('API Error:', error);
                return Promise.reject(error);
            }
        );

        /**
         * Add bearer token to request if the user is logged in.
         */
        this.http.interceptors.request.use(config => {
            if(localStorage.getItem("is_authenticated"))
            {
                const token = localStorage.getItem('auth_token');
                if (token) {
                    config.headers.Authorization = `Bearer ${token}`;
                }
            }
            return config;
        });

    }

    public static getInstance(): ApiService {
        if (!ApiService.instance) {
            ApiService.instance = new ApiService();
        }
        return ApiService.instance;
    }


    public async getAllBooks(startIndex: number, numResultsToGet: number): Promise<BookListDTO> {
        const response = await this.http.get<BookListDTO>(`/api/v1/books/get/all?startIndex=${startIndex}&numResultsToGet=${numResultsToGet}`);
        return response.data;
    }

    public async getBookByID(bookID: string): Promise<BookDTO> {
        const response = await this.http.get<BookDTO>(`/api/v1/books/get/byID/${bookID}`);
        return response.data;
    }

    public async getKeywordSearch(keywords: string, startIndex: number, numResultsToGet: number) {
        keywords = keywords.replace(/\s+/g, "+");
        const response = await this.http.get<BookListDTO>(`/api/v1/search/external/keyword?keywords=${keywords}&startIndex=${startIndex}&numResultsToGet=${numResultsToGet}`);
        return response.data;
    }

    public async authenticate(username: string, password: string): Promise<number> {
        let authRequestDTO : AuthRequestDTO = {username: username, password: password}

        try {
            const result = await this.http.post("/api/v1/auth/authenticate", authRequestDTO);
            localStorage.setItem("is_authenticated", "true");
            localStorage.setItem("username", username);
            localStorage.setItem("auth_token", result.data);

            return result.status;
        } catch (error) {
            if (axios.isAxiosError(error) && error.response?.status === 403) {
                return 403;
            } else {
                throw error;
            }
        }
    }

    public async signUp(username: string, password: string): Promise<number>{
        let authRequestDTO : AuthRequestDTO = {username: username, password: password};
        try{
            const result = await this.http.post("/api/v1/auth/add-user", authRequestDTO);
            return result.status;
        }catch (error){
            if (axios.isAxiosError(error) && error.response?.status === 409) {
                return 409;
            } else {
                throw error;
            }
        }
    }

    public async getLibrary(startIndex: number, numResultsToGet: number){
        const result = await this.http.get<BookListDTO>(`/api/v1/books/get/library?startIndex=${startIndex}&numResultsToGet=${numResultsToGet}`);
        return result.data;
    }

    public async getWishlist(startIndex: number, numResultsToGet: number){
        const result = await this.http.get<BookListDTO>(`/api/v1/books/get/wishlist?startIndex=${startIndex}&numResultsToGet=${numResultsToGet}`);
        return result.data;
    }

    public async addBookToLibrary(bookID : string) {
        let addRequest : AddBookRequestDTO = {bookID: bookID};
        await this.http.post("/api/v1/books/add/library", addRequest);

    }

    public async addBookToWishlist(bookID : string){
        let addRequest : AddBookRequestDTO = {bookID: bookID};
        await this.http.post("/api/v1/books/add/wishlist", addRequest);
        // Throws an error if something is wrong
    }

    public async deleteBookFromLibrary(bookID : string) {
        await this.http.delete(`/api/v1/books/delete/library/${bookID}`);
        // Throws an error if something is wrong
    }

    public async deleteBookFromWishlist(bookID : string){
        await this.http.delete(`/api/v1/books/delete/wishlist/${bookID}`);
        // Throws an error if something is wrong
    }

    public async updateRating(bookID : string, rating : number){
        let ratingDTO : ChangeBookRatingDTO = {bookID: bookID, rating: rating};
        await this.http.put(`/api/v1/books/update/rating`, ratingDTO);
    }

    public async updateStatus(bookID : string, status : ReadingStatusType){
        let statusDTO : ChangeBookReadingStatusRequestDTO = {bookID : bookID, status : status};
        await this.http.put(`/api/v1/books/update/status`, statusDTO);
    }

// Add more methods here as needed
}

export const apiService = ApiService.getInstance();