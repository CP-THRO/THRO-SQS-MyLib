package de.throsenheim.inf.sqs.christophpircher.mylibbackend.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Class to parse the author name from the author API with Jackson
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true) // To ignore everything not explicitly specified
public class OpenLibraryAPIAuthor {

    /**
     * Name of the author
     */
    @JsonProperty("name")
    private String name;
}
