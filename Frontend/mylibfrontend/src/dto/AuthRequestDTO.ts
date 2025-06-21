/**
 * Data Transfer Object (DTO) representing an authentication request.
 *
 * Used for both user registration and login endpoints, where a client provides
 * a username and password to authenticate or create an account.
 */
export interface AuthRequestDTO {
    /**
     * The username to be submitted
     */
    username: string;

    /**
     * The password to be submitted
     */
    password: string;
}