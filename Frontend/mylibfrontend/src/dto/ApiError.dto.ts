/**
 * Data Transfer Object (DTO) representing a structured API error response.
 *
 * This structure is used to return consistent and descriptive error information
 * when exceptions occur. It includes the HTTP status, a general message,
 * and a list of specific error descriptions.
 */
export interface ApiError {
    /**
     * The HTTP status of the error response (e.g., "BAD_REQUEST").
     */
    status: string; // Typically a string like "BAD_REQUEST" or numeric code like 400

    /**
     * A general message describing the exception.
     */
    message: string;

    /**
     * A list of individual error messages.
     */
    errors: string[];
}