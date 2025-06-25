import { defineConfig } from 'vitest/config';
import vue from '@vitejs/plugin-vue';

export default defineConfig({
    plugins: [vue()],
    test: {
        globals: true,
        environment: 'jsdom',
        coverage: {
            reporter: ['text', 'lcov'], // text for CLI, lcov for SonarQube
            reportsDirectory: './coverage',
            exclude: ['**/node_modules/**', '**/tests/**'],
        },
    } as any, //supress warning
});