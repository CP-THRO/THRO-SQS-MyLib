// tests/factories/book.ts
import type { BookDTO } from '../../dto/BookDTO';

export function createBookDTO(overrides: Partial<BookDTO> = {}): BookDTO {
    return {
        bookID: 'OL9698350M',
        title: 'The Great Book',
        subtitle: 'A Subtitle',
        authors: ['Author One', 'Author Two'],
        description: 'A long and compelling description.',
        isbns: ['1234567890', '0987654321'],
        coverURLSmall: 'https://example.com/small.jpg',
        coverURLMedium: 'https://example.com/medium.jpg',
        coverURLLarge: 'https://example.com/large.jpg',
        publishDate: '2001-01-01',
        individualRating: 4,
        averageRating: 4.3,
        readingStatus: "READING",
        bookIsInLibrary: true,
        bookIsOnWishlist: false,
        ...overrides,
    };
}