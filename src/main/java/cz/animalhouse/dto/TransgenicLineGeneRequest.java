package cz.animalhouse.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TransgenicLineGeneRequest(

        @NotNull(message = "Transgenic line ID is required")
        @Positive(message = "Transgenic line ID must be positive")
        Long transgenicLineId,

        @NotNull(message = "Gene ID is required")
        @Positive(message = "Gene ID must be positive")
        Long geneId

) {
}