package de.throsenheim.inf.sqs.christophpircher.mylibbackend.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;


/**
 *  Class to parse the description from the works API for a work with Jackson.
 */
@Data
public class OpenLibraryAPIWork {

    @JsonProperty("description")
    private Description description;

    @Data
    @JsonDeserialize(using = DescriptionDeserializer.class)
    public static class Description{

        /**
         * Type. Always "/type/text"
         */
        @JsonProperty("type")
        private String type;

        /**
         * Actual text of the description. This is what I want.
         */
        @JsonProperty("value")
        private String value;
    }

}
