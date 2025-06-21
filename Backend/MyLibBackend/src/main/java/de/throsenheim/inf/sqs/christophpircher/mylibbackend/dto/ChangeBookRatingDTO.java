package de.throsenheim.inf.sqs.christophpircher.mylibbackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

/**
 * Data Transfer Object (DTO) used to update the user's rating for a specific book.
 * <p>
 * This object is typically used in HTTP PUT requests to update book metadata within the user's personal library.
 * </p>
 *
 * <p>Validation is applied to ensure both fields are present and correctly formatted.</p>
 *
 * <p>This class is used in the endpoint:</p>
 * <ul>
 *   <li>PUT /update/rating</li>
 * </ul>
 *
 * @see de.throsenheim.inf.sqs.christophpircher.mylibbackend.controller.BookController
 */
@Data
@Builder
public class ChangeBookRatingDTO {

    /**
     * The OpenLibrary book identifier (e.g. "OL9698350M")
     * <p>
     * This ID is used to identify which book should be updated in the user's library.
     * </p>
     *
     */
    @JsonProperty("bookID")
    @Schema(description = "OpenLibrary book ID", example = "OL9698350M")
    @NotNull
    @NotBlank
    @NotEmpty
    private String bookID;

    /**
     * The user-assigned rating for the book.
     * <p>
     * Must be an integer between 1 and 5 (inclusive).
     * </p>
     */
    @JsonProperty("rating")
    @Schema(description = "The rating of the book between 1 and 5", example = "4")
    @Range(min = 1, max = 5)
    @NotNull
    @NotBlank
    @NotEmpty
    private int rating;
}
