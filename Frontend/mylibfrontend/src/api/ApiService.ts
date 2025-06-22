import axios from 'axios'
import type {AxiosInstance} from 'axios';
import type {BookDTO} from "../dto/BookDTO.ts";
import type {BookListDTO} from "../dto/BookListDTO.ts";

class ApiService {
    private static instance: ApiService;
    private http: AxiosInstance;

    private constructor() {
        this.http = axios.create({
            baseURL: 'http://localhost:8080', // Placeholder for now
            headers: {
                'Content-Type': 'application/json',
            },
        });

        /*
        // Optional: Add interceptors
        this.http.interceptors.response.use(
            (response) => response,
            (error) => {
                console.error('API Error:', error);
                return Promise.reject(error);
            }
        );

        */

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

    public async getBookByID(bookID : string): Promise<BookDTO> {
        const response = await this.http.get<BookDTO>(`/api/v1/books/get/byID/${bookID}`);
        return response.data;
    }

    public async getKeywordSearch(keywords : string, startIndex: number, numResultsToGet: number){
        keywords = keywords.replace(/\s+/g, "+");
        console.log(keywords)
        const response = await this.http.get<BookListDTO>(`/api/v1/search/external/keyword?keywords=${keywords}&startIndex=${startIndex}&numResultsToGet=${numResultsToGet}`);
        return response.data;
    }


    // Example method to fetch users
//public async getUsers(): Promise<UserDto[]> {
//        const response = await this.http.get('/users');
//        return response.data.map((userData: any) => new UserDto(userData));
//}

// Add more methods here as needed
}

export const apiService = ApiService.getInstance();