// tests/factories/auth.ts
import type { AuthRequestDTO } from '../../src/dto/AuthRequestDTO.ts';

export function createAuthRequestDTO(overrides: Partial<AuthRequestDTO> = {}): AuthRequestDTO {
    return {
        username: 'testuser',
        password: 'password123',
        ...overrides,
    };
}