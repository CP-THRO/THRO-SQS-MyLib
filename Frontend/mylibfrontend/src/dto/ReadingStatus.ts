/**
 * Enum-like object representing possible reading statuses for a book.
 *
 * Used to track a user's progress or intent regarding a specific title.
 */
export const ReadingStatus = {
    UNREAD: 'UNREAD',
    READING: 'READING',
    READ: 'READ',
} as const;

/**
 * Type representing one of the allowed reading status values.
 *
 * Resolves to a union of the string literals: "UNREAD" | "READING" | "READ".
 *
 * Needed for passing this as a value as type of ReadingStatus
 */
export type ReadingStatusType = typeof ReadingStatus[keyof typeof ReadingStatus];