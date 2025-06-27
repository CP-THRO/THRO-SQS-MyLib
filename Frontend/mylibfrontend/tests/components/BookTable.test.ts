import { describe, it, expect, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import BookTable from '../../src/components/BookTable.vue';
import { BookDTO } from '../../src/dto/BookDTO';

const sampleBooks: BookDTO[] = [
    {
        bookID: 'b1',
        title: 'Sample Title',
        subtitle: '',
        description: '',
        authors: ['Author A'],
        publishDate: '2020',
        isbns: ['123'],
        averageRating: 4,
        individualRating: 3,
        readingStatus: 'UNREAD',
        bookIsInLibrary: true,
        bookIsOnWishlist: false,
        coverURLLarge: '',
    },
];

const columns = [
    { label: 'Title', field: 'title' },
    { label: 'Authors', field: 'authors' },
];

describe('BookTable.vue', () => {
    it('renders headers from columns', () => {
        const wrapper = mount(BookTable, {
            props: {
                books: sampleBooks,
                columns,
                currentPage: 1,
                totalPages: 2,
                pageSize: 10,
                pageSizes: [10, 20],
            },
        });

        const headers = wrapper.findAll('th').map(th => th.text());
        expect(headers).toEqual(['Title', 'Authors']);
    });

    it('renders book data based on columns', () => {
        const wrapper = mount(BookTable, {
            props: {
                books: sampleBooks,
                columns,
                currentPage: 1,
                totalPages: 2,
                pageSize: 10,
                pageSizes: [10, 20],
            },
        });

        const rowCells = wrapper.findAll('tbody td').map(td => td.text());
        expect(rowCells[0]).toBe('Sample Title');
        expect(rowCells[1]).toContain('Author A');
    });

    it('emits next-page and prev-page events', async () => {
        const wrapper = mount(BookTable, {
            props: {
                books: sampleBooks,
                columns,
                currentPage: 2,
                totalPages: 3,
                pageSize: 10,
                pageSizes: [10, 20],
            },
        });

        const nextBtn = wrapper.findAll('button').find(btn => btn.text().includes('Next'));
        const prevBtn = wrapper.findAll('button').find(btn => btn.text().includes('Previous'));

        expect(nextBtn).toBeTruthy();
        expect(prevBtn).toBeTruthy();

        await nextBtn!.trigger('click');
        await prevBtn!.trigger('click');

        expect(wrapper.emitted('next-page')).toBeTruthy();
        expect(wrapper.emitted('prev-page')).toBeTruthy();
    });

    it('renders custom slot if defined', () => {
        const wrapper = mount(BookTable, {
            props: {
                books: sampleBooks,
                columns: [
                    { label: 'Custom', field: 'bookID', slot: 'custom-col' },
                ],
                currentPage: 1,
                totalPages: 1,
                pageSize: 10,
                pageSizes: [10, 20],
            },
            slots: {
                'custom-col': `<template #custom-col="{ book }"><span class="custom-slot">{{ book.bookID }}</span></template>`,
            },
        });

        expect(wrapper.find('.custom-slot').text()).toBe('b1');
    });
});
