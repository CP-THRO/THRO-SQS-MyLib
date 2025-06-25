// tests/factories/auth.ts
import type { AuthRequestDTO } from '../../dto/AuthRequestDTO';

export function createAuthRequestDTO(overrides: Partial<AuthRequestDTO> = {}): AuthRequestDTO {
    return {
        username: 'testuser',
        password: 'password123',
        ...overrides,
    };
}