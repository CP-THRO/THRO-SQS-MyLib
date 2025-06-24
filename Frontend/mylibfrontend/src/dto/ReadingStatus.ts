export const ReadingStatus = {
    UNREAD: 'UNREAD',
    READING: 'READING',
    READ: 'READ',
} as const;

export type ReadingStatusType = typeof ReadingStatus[keyof typeof ReadingStatus];