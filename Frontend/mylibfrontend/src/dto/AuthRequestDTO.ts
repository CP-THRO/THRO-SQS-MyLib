/**
 * Data Transfer Object (DTO) representing an authentication request.
 *
 * Used for user registration and login, where the client submits
 * credentials for account creation or verification.
 */
export interface AuthRequestDTO {
    /**
     * The username provided by the user.
     * Required for both login and registration.
     */
    username: string;

    /**
     * The user's password.
     * Used to verify identity or secure a new account.
     */
    password: string;
}