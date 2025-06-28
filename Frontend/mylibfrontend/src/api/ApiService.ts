import axios from 'axios';
import type { AxiosInstance } from 'axios';
import type { BookDTO } from "../dto/BookDTO.ts";
import type { BookListDTO } from "../dto/BookListDTO.ts";
import type { AuthRequestDTO } from "../dto/AuthRequestDTO.ts";
import type { AddBookRequestDTO } from "../dto/AddBookRequestDTO.ts";
import type { ChangeBookRatingDTO } from "../dto/ChangeBookRatingDTO.ts";
import type { ReadingStatusType } from "../dto/ReadingStatus.ts";
import type { ChangeBookReadingStatusRequestDTO } from "../dto/ChangeBookReadingStatus.dto.ts";
import {config} from "../wrapper/AppConfig.ts";

/**
 * Singleton service for making HTTP requests to the backend API.
 *
 * Handles authentication, book management, and user-related operations.
 */
export class ApiService {
    private static instance: ApiService;
    private readonly http: AxiosInstance;

    private constructor() {
        console.log(config.BACKEND_HOST)
        console.log(config.BACKEND_PORT)

        this.http = axios.create({
            baseURL: `${config.BACKEND_HOST}:${config.BACKEND_PORT}`,
            headers: {
                'Content-Type': 'application/json',
            },
        });

        // Global response interceptor for error handling
        this.http.interceptors.response.use(
            (response) => response,
            handleApiError
        );

        // Request interceptor to attach bearer token if user is authenticated
        this.http.interceptors.request.use(attachAuthToken);
    }

    // To load config dynamically at startup. Must happen AFTER the config is loaded. Therefore I can't use an exported instance
    public static init() {
        if (!ApiService.instance) {
            ApiService.instance = new ApiService();
        }
    }

    /**
     * Returns the singleton instance of ApiService.
     */
    public static getInstance(): ApiService {
        if (!ApiService.instance) {
            throw new Error("ApiService not initialized!");
        }
        return ApiService.instance;
    }

    /**
     * Fetches all books with pagination.
     */
    public async getAllBooks(startIndex: number, numResultsToGet: number): Promise<BookListDTO> {
        const response = await this.http.get<BookListDTO>(
            `/api/v1/books/get/all?startIndex=${startIndex}&numResultsToGet=${numResultsToGet}`
        );
        return response.data;
    }

    /**
     * Fetches a specific book by its OpenLibrary ID.
     */
    public async getBookByID(bookID: string): Promise<BookDTO> {
        const response = await this.http.get<BookDTO>(`/api/v1/books/get/byID/${bookID}`);
        return response.data;
    }

    /**
     * Performs a keyword-based book search with pagination.
     */
    public async getKeywordSearch(keywords: string, startIndex: number, numResultsToGet: number) {
        keywords = keywords.replace(/\s+/g, "+");
        const response = await this.http.get<BookListDTO>(
            `/api/v1/search/external/keyword?keywords=${keywords}&startIndex=${startIndex}&numResultsToGet=${numResultsToGet}`
        );
        return response.data;
    }

    /**
     * Authenticates a user and stores their JWT token.
     */
    public async authenticate(username: string, password: string): Promise<number> {
        const authRequestDTO: AuthRequestDTO = { username, password };

        try {
            const result = await this.http.post("/api/v1/auth/authenticate", authRequestDTO);
            localStorage.setItem("is_authenticated", "true");
            localStorage.setItem("username", username);
            localStorage.setItem("auth_token", result.data);
            return result.status;
        } catch (error) {
            if (axios.isAxiosError(error) && error.response?.status === 403) {
                return 403;
            }
            throw error;
        }
    }

    /**
     * Registers a new user account.
     */
    public async signUp(username: string, password: string): Promise<number> {
        const authRequestDTO: AuthRequestDTO = { username, password };

        try {
            const result = await this.http.post("/api/v1/auth/add-user", authRequestDTO);
            return result.status;
        } catch (error) {
            if (axios.isAxiosError(error) && error.response?.status === 409) {
                return 409;
            }
            throw error;
        }
    }

    /**
     * Retrieves the user's library (paginated).
     */
    public async getLibrary(startIndex: number, numResultsToGet: number) {
        const result = await this.http.get<BookListDTO>(
            `/api/v1/books/get/library?startIndex=${startIndex}&numResultsToGet=${numResultsToGet}`
        );
        return result.data;
    }

    /**
     * Retrieves the user's wishlist (paginated).
     */
    public async getWishlist(startIndex: number, numResultsToGet: number) {
        const result = await this.http.get<BookListDTO>(
            `/api/v1/books/get/wishlist?startIndex=${startIndex}&numResultsToGet=${numResultsToGet}`
        );
        return result.data;
    }

    /**
     * Adds a book to the user's library.
     */
    public async addBookToLibrary(bookID: string) {
        const addRequest: AddBookRequestDTO = { bookID };
        await this.http.post("/api/v1/books/add/library", addRequest);
    }

    /**
     * Adds a book to the user's wishlist.
     */
    public async addBookToWishlist(bookID: string) {
        const addRequest: AddBookRequestDTO = { bookID };
        await this.http.post("/api/v1/books/add/wishlist", addRequest);
    }

    /**
     * Removes a book from the user's library.
     */
    public async deleteBookFromLibrary(bookID: string) {
        await this.http.delete(`/api/v1/books/delete/library/${bookID}`);
    }

    /**
     * Removes a book from the user's wishlist.
     */
    public async deleteBookFromWishlist(bookID: string) {
        await this.http.delete(`/api/v1/books/delete/wishlist/${bookID}`);
    }

    /**
     * Updates the user's rating for a book.
     */
    public async updateRating(bookID: string, rating: number) {
        const ratingDTO: ChangeBookRatingDTO = { bookID, rating };
        await this.http.put(`/api/v1/books/update/rating`, ratingDTO);
    }

    /**
     * Updates the user's reading status for a book.
     */
    public async updateStatus(bookID: string, status: ReadingStatusType) {
        const statusDTO: ChangeBookReadingStatusRequestDTO = { bookID, status };
        await this.http.put(`/api/v1/books/update/status`, statusDTO);
    }
}

export function handleApiError(error: any) { //exported as its own function for unit testing
    console.error('API Error:', error);
    return Promise.reject(error);
}

export function attachAuthToken(config: any) { // exported as its own function for unit testing
    if (localStorage.getItem("is_authenticated")) {
        const token = localStorage.getItem("auth_token");
        if (token) {
            config.headers = config.headers ?? {};
            config.headers.Authorization = `Bearer ${token}`;
        }
    }
    return config;
}