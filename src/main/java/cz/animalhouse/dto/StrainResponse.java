package cz.animalhouse.dto;

import cz.animalhouse.entity.Strain;

public record StrainResponse(
        Long id,
        String code,
        String name
) {

    public static StrainResponse fromEntity(Strain strain) {
        return new StrainResponse(
                strain.getId(),
                strain.getCode(),
                strain.getName()
        );
    }
}
