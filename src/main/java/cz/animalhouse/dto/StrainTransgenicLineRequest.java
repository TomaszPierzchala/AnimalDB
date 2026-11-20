package cz.animalhouse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StrainTransgenicLineRequest(

        @NotNull(message = "Strain ID is required")
        Long strainId,

        @Size(
            max = 100,
            message = "Transgenic line name cannot exceed 100 characters"
        )
        String name

) {
}