package cz.animalhouse.dto;

import cz.animalhouse.entity.Gene;

public class GeneResponse {

    private Long id;
    private String symbol;
    private String description;

    public GeneResponse(Long id, String symbol, String description) {
        this.id = id;
        this.symbol = symbol;
        this.description = description;
    }

    public static GeneResponse fromEntity(Gene gene) {
        return new GeneResponse(
                gene.getId(),
                gene.getSymbol(),
                gene.getDescription());
    }

    public Long getId() {
        return id;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getDescription() {
        return description;
    }
}