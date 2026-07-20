package cz.animalhouse.dto;

import cz.animalhouse.entity.TransgenicLine;

public record TransgenicLineResponse(
        Long id,
        Long strainId,
        String strainCode,
        String strainName,
        String name
) {

    public static TransgenicLineResponse fromEntity(
            TransgenicLine transgenicLine) {

        return new TransgenicLineResponse(
                transgenicLine.getId(),
                transgenicLine.getStrain().getId(),
                transgenicLine.getStrain().getCode(),
                transgenicLine.getStrain().getName(),
                transgenicLine.getName()
        );
    }
}