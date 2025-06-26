// tests/factories/bookList.ts
import type { BookListDTO } from '../../src/dto/BookListDTO.ts';
import { createBookDTO } from './book.ts';

export function createBookListDTO(overrides: Partial<BookListDTO> = {}): BookListDTO {
    return {
        numResults: 2,
        startIndex: 0,
        skippedBooks: 0,
        books: [
            createBookDTO({ bookID: 'OL1M', title: 'Book One' }),
            createBookDTO({ bookID: 'OL2M', title: 'Book Two' }),
        ],
        ...overrides,
    };
}
