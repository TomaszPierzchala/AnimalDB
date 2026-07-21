package cz.animalhouse.dto;

import cz.animalhouse.entity.Gene;

public record GeneResponse(
        Long id,
        String symbol,
        String description
) {

    public static GeneResponse fromEntity(Gene gene) {
        return new GeneResponse(
                gene.getId(),
                gene.getSymbol(),
                gene.getDescription()
        );
    }
}
