package de.throsenheim.inf.sqs.christophpircher.mylibbackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.ReadingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Data Transfer Object (DTO) used to update the reading status of a specific book
 * in the user's personal library.
 * <p>
 * This object is used in HTTP PUT requests to update the {@link ReadingStatus} of a book,
 * such as marking it as "READ", "READING", or "WISHLIST".
 * </p>
 *
 * @see de.throsenheim.inf.sqs.christophpircher.mylibbackend.model.ReadingStatus
 * @see de.throsenheim.inf.sqs.christophpircher.mylibbackend.controller.BookController
 */
@Data
@AllArgsConstructor //For unit test
public class ChangeBookReadingStatusRequestDTO {

    /**
     * The OpenLibrary book identifier (e.g. "OL9698350M")
     * <p>
     * This ID is used to identify which book should be updated in the user's library.
     * </p>
     *
     */
    @JsonProperty("bookID")
    @Schema(description = "OpenLibrary book ID", example = "OL9698350M")
    @NotBlank
    @NotEmpty
    @NotNull
    private String bookID;

    /**
     * The new reading status to apply to the book.
     * <p>
     * Must be a valid value of the {@link ReadingStatus} enum.
     * </p>
     *
     * @see ReadingStatus
     */
    @JsonProperty("status")
    @Schema(description = "New reading status of the book")
    @NotNull
    @NotEmpty
    @NotBlank
    private ReadingStatus status;

}
