import { describe, it, expect, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import BaseBookList from '../../src/components/BaseBookList.vue';
import { createBookDTO } from '../factories/book';

const createBookListDTO = (books) => ({
    numResults: books.length,
    skippedBooks: 0,
    startIndex: 0,
    books,
});

const BookTableStub = {
    template: `
    <div>
      <slot name="title" :book="book" />
      <slot name="cover" :book="book" />
      <slot name="authors" :book="book" />
    </div>
  `,
    props: ['books'],
    computed: {
        book() {
            return this.books?.[0] || {};
        }
    }
};


describe('BaseBookList.vue', () => {
    const defaultProps = {
        title: 'Test Book List',
        books: createBookListDTO([createBookDTO()]),
        loading: false,
        error: null,
        columns: ['title', 'authors', 'publishDate'],
        currentPage: 1,
        totalPages: 1,
        pageSize: 10,
        pageSizes: [5, 10, 25, 50],
    };

    it('renders title', () => {
        const wrapper = mount(BaseBookList, { props: defaultProps });
        expect(wrapper.text()).toContain('Test Book List');
    });

    it('shows loading message', () => {
        const wrapper = mount(BaseBookList, {
            props: { ...defaultProps, loading: true },
        });
        expect(wrapper.text()).toContain('Loading...');
    });

    it('shows error message', () => {
        const wrapper = mount(BaseBookList, {
            props: { ...defaultProps, error: 'Something went wrong' },
        });
        expect(wrapper.text()).toContain('Something went wrong');
    });

    it('shows no books message when numResults is 0', () => {
        const wrapper = mount(BaseBookList, {
            props: {
                ...defaultProps,
                books: { ...defaultProps.books, numResults: 0 },
            },
        });
        expect(wrapper.text()).toContain('No books found.');
    });

    it('renders BookTable when books are provided', () => {
        const wrapper = mount(BaseBookList, { props: defaultProps });
        expect(wrapper.findComponent({ name: 'BookTable' }).exists()).toBe(true);
    });

    it('renders slot content: title + subtitle', () => {
        const book = createBookDTO();
        const wrapper = mount(BaseBookList, {
            props: {
                ...defaultProps,
                books: createBookListDTO([book]),
            },
            global: {
                components: {
                    BookTable: BookTableStub,
                },
            },
            slots: {
                title: `<template #title="{ book }">{{ book.title }} - {{ book.subtitle }}</template>`,
            },
        });

        expect(wrapper.html()).toContain(`${book.title} - ${book.subtitle}`);
    });


    it('emits next-page event', async () => {
        const wrapper = mount(BaseBookList, { props: defaultProps });
        await wrapper.findComponent({ name: 'BookTable' }).vm.$emit('next-page');
        expect(wrapper.emitted('next-page')).toBeTruthy();
    });

    it('emits prev-page event', async () => {
        const wrapper = mount(BaseBookList, { props: defaultProps });
        await wrapper.findComponent({ name: 'BookTable' }).vm.$emit('prev-page');
        expect(wrapper.emitted('prev-page')).toBeTruthy();
    });

    it('emits page-size-change event with payload', async () => {
        const wrapper = mount(BaseBookList, { props: defaultProps });
        await wrapper.findComponent({ name: 'BookTable' }).vm.$emit('page-size-change', 25);
        expect(wrapper.emitted('page-size-change')).toEqual([[25]]);
    });
});
