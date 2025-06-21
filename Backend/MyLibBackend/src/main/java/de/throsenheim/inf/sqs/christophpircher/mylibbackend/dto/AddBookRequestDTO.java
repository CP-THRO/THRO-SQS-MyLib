package de.throsenheim.inf.sqs.christophpircher.mylibbackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
/**
 * Data Transfer Object (DTO) used for requests to add a book to a user's library or wishlist.
 * <p>
 * This object contains the unique OpenLibrary book identifier needed to perform the operation.
 * </p>
 * <p>Validation annotations ensure that the book ID is not null, blank, or empty.</p>
 * @see de.throsenheim.inf.sqs.christophpircher.mylibbackend.controller.BookController
 */
public class AddBookRequestDTO {

    /**
     * The OpenLibrary book identifier (e.g., "OL9698350M").
     * <p>
     * This ID is required to locate and add the book to the user's collection or wishlist.
     * </p>
     */
    @JsonProperty("bookID")
    @Schema(description = "OpenLibrary book ID", example = "OL9698350M")
    @NotBlank
    @NotEmpty
    @NotNull
    private String bookID;
}
