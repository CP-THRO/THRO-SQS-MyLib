package de.throsenheim.inf.sqs.christophpircher.mylibbackend.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Class to parse the author name from the author API with Jackson
 */
@Data
public class OpenLibraryAPIAuthor {

    @JsonProperty("name")
    private String name;
}
