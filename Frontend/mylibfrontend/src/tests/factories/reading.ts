// tests/factories/reading.ts
import type { ChangeBookReadingStatusRequestDTO } from '../../dto/ChangeBookReadingStatus.dto.ts';

export function createChangeBookReadingStatusRequestDTO(
    overrides: Partial<ChangeBookReadingStatusRequestDTO> = {}
): ChangeBookReadingStatusRequestDTO {
    return {
        bookID: 'OL9698350M',
        status: 'READING', // default example
        ...overrides,
    };
}