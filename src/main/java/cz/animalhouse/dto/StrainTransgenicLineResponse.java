package cz.animalhouse.dto;

import cz.animalhouse.entity.StrainTransgenicLine;

public record StrainTransgenicLineResponse(
        Long id,
        Long strainId,
        String strainCode,
        String strainName,
        String name
) {

    public static StrainTransgenicLineResponse fromEntity(
            StrainTransgenicLine transgenicLine) {

        return new StrainTransgenicLineResponse(
                transgenicLine.getId(),
                transgenicLine.getStrain().getId(),
                transgenicLine.getStrain().getCode(),
                transgenicLine.getStrain().getName(),
                transgenicLine.getName()
        );
    }
}