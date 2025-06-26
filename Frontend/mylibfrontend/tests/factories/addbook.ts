// tests/factories/addbook.ts
import type { AddBookRequestDTO } from '../../src/dto/AddBookRequestDTO.ts';

export function createAddBookRequestDTO(overrides: Partial<AddBookRequestDTO> = {}): AddBookRequestDTO {
    return {
        bookID: 'OL1234567M',
        ...overrides,
    };
}