// tests/api/ApiService.test.ts
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import axios from 'axios';
import type { AxiosInstance } from 'axios';
import { createBookDTO } from '../factories/book.ts'; // adjust path if needed


vi.mock('axios');

const mockUse = vi.fn();
const mockHttp = {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
    interceptors: {
        request: { use: mockUse },
        response: { use: mockUse },
    },
};

let apiService: any;

beforeEach(async () => {
    (axios.create as any) = vi.fn().mockReturnValue(mockHttp as unknown as AxiosInstance);
    (axios.isAxiosError as any) = vi.fn((err) => !!err?.isAxiosError); // Because I need this to handle the check on error in auth and sign-up
    mockUse.mockClear();

    const module = await import('../../src/api/ApiService.ts');
    apiService = module.apiService;
});

afterEach(() => {
    vi.clearAllMocks(); // resets spies
    mockHttp.get.mockReset();
    mockHttp.post.mockReset();
    mockHttp.put.mockReset();
    mockHttp.delete.mockReset();
    mockUse.mockReset();
});

import type { BookListDTO } from '../../src/dto/BookListDTO.ts';


describe('ApiService', () => {
    it('fetches all books', async () => {
        const fakeData: BookListDTO = { numResults: 1, startIndex: 0, books: [], skippedBooks: 0 };
        mockHttp.get.mockResolvedValue({ data: fakeData });

        const result = await apiService.getAllBooks(0, 10);
        expect(mockHttp.get).toHaveBeenCalledWith('/api/v1/books/get/all?startIndex=0&numResultsToGet=10');
        expect(result).toEqual(fakeData);
    });

    it('authenticates successfully and stores token', async () => {
        const mockToken = 'token123';
        mockHttp.post.mockResolvedValue({ data: mockToken, status: 200 });

        const status = await apiService.authenticate('user', 'pass');

        expect(mockHttp.post).toHaveBeenCalledWith('/api/v1/auth/authenticate', {
            username: 'user',
            password: 'pass',
        });
        expect(localStorage.getItem('auth_token')).toBe(mockToken);
        expect(localStorage.getItem('is_authenticated')).toBe('true');
        expect(status).toBe(200);
    });

    it('returns 403 on failed auth', async () => {
        mockHttp.post.mockRejectedValue({ response: { status: 403 }, isAxiosError: true });

        const status = await apiService.authenticate('bad', 'user');
        expect(status).toBe(403);
    });

    it('throws non-403 errors during auth', async () => {
        mockHttp.post.mockRejectedValue(new Error('network'));

        await expect(apiService.authenticate('user', 'pass')).rejects.toThrow('network');
    });

    it('signs up a user', async () => {
        mockHttp.post.mockResolvedValue({ status: 201 });

        const status = await apiService.signUp('newUser', '123');
        expect(mockHttp.post).toHaveBeenCalledWith('/api/v1/auth/add-user', {
            username: 'newUser',
            password: '123',
        });
        expect(status).toBe(201);
    });

    it('returns 409 on sign-up conflict', async () => {
        mockHttp.post.mockRejectedValue({ response: { status: 409 }, isAxiosError: true });

        const status = await apiService.signUp('existing', 'pass');
        expect(status).toBe(409);
    });

    it('fetches library and wishlist', async () => {
        mockHttp.get.mockResolvedValue({ data: 'libraryData' });
        const library = await apiService.getLibrary(0, 10);
        expect(mockHttp.get).toHaveBeenCalledWith('/api/v1/books/get/library?startIndex=0&numResultsToGet=10');
        expect(library).toBe('libraryData');

        mockHttp.get.mockResolvedValue({ data: 'wishlistData' });
        const wishlist = await apiService.getWishlist(5, 5);
        expect(mockHttp.get).toHaveBeenCalledWith('/api/v1/books/get/wishlist?startIndex=5&numResultsToGet=5');
        expect(wishlist).toBe('wishlistData');
    });

    it('adds and removes books from library and wishlist', async () => {
        await apiService.addBookToLibrary('OL1');
        expect(mockHttp.post).toHaveBeenCalledWith('/api/v1/books/add/library', { bookID: 'OL1' });

        await apiService.addBookToWishlist('OL2');
        expect(mockHttp.post).toHaveBeenCalledWith('/api/v1/books/add/wishlist', { bookID: 'OL2' });

        await apiService.deleteBookFromLibrary('OL3');
        expect(mockHttp.delete).toHaveBeenCalledWith('/api/v1/books/delete/library/OL3');

        await apiService.deleteBookFromWishlist('OL4');
        expect(mockHttp.delete).toHaveBeenCalledWith('/api/v1/books/delete/wishlist/OL4');
    });

    it('updates book rating and reading status', async () => {
        await apiService.updateRating('OL5', 5);
        expect(mockHttp.put).toHaveBeenCalledWith('/api/v1/books/update/rating', {
            bookID: 'OL5',
            rating: 5,
        });

        await apiService.updateStatus('OL6', 'READING');
        expect(mockHttp.put).toHaveBeenCalledWith('/api/v1/books/update/status', {
            bookID: 'OL6',
            status: 'READING',
        });
    });

    it('searches books by keyword', async () => {
        mockHttp.get.mockResolvedValue({ data: 'searchResults' });
        const result = await apiService.getKeywordSearch('harry potter', 0, 10);
        expect(mockHttp.get).toHaveBeenCalledWith('/api/v1/search/external/keyword?keywords=harry+potter&startIndex=0&numResultsToGet=10');
        expect(result).toBe('searchResults');
    });


    it('logs and rethrows API errors', async () => {
        const spy = vi.spyOn(console, 'error').mockImplementation(() => {});
        const err = new Error('boom');

        const { handleApiError } = await import('../../src/api/ApiService.ts');

        await expect(handleApiError(err)).rejects.toThrow('boom');
        expect(spy).toHaveBeenCalledWith('API Error:', err);

        spy.mockRestore();
    });

    it('adds Authorization header if user is authenticated', async () => {
        localStorage.setItem('is_authenticated', 'true');
        localStorage.setItem('auth_token', 'abc123');

        const { attachAuthToken } = await import('../../src/api/ApiService.ts');

        const result = attachAuthToken({ headers: {} });

        expect(result.headers.Authorization).toBe('Bearer abc123');

        localStorage.clear();
    });

    it('does not add Authorization header if not authenticated', async () => {
        localStorage.removeItem('is_authenticated');
        localStorage.removeItem('auth_token');

        const { attachAuthToken } = await import('../../src/api/ApiService.ts');

        const result = attachAuthToken({ headers: {} });

        expect(result.headers.Authorization).toBeUndefined();
    });


    it('fetches a book by ID', async () => {
        const fakeBook = createBookDTO();
        mockHttp.get.mockResolvedValueOnce({ data: fakeBook });

        const result = await apiService.getBookByID(fakeBook.bookID);

        expect(mockHttp.get).toHaveBeenCalledWith(`/api/v1/books/get/byID/${fakeBook.bookID}`);
        expect(result).toEqual(fakeBook);
    });

    it('throws non-Axios errors during sign up', async () => {
        const error = new Error('unexpected error');
        mockHttp.post.mockRejectedValueOnce(error);

        await expect(apiService.signUp('any', 'thing')).rejects.toThrow('unexpected error');
    });


});