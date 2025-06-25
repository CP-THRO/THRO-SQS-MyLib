// tests/factories/addbook.ts
import type { AddBookRequestDTO } from '../../dto/AddBookRequestDTO';

export function createAddBookRequestDTO(overrides: Partial<AddBookRequestDTO> = {}): AddBookRequestDTO {
    return {
        bookID: 'OL1234567M',
        ...overrides,
    };
}