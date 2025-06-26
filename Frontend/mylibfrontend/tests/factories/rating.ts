// tests/factories/rating.ts
import type { ChangeBookRatingDTO } from '../../src/dto/ChangeBookRatingDTO.ts';

export function createChangeBookRatingDTO(overrides: Partial<ChangeBookRatingDTO> = {}): ChangeBookRatingDTO {
    return {
        bookID: 'OL9698350M',
        rating: 4,
        ...overrides,
    };
}