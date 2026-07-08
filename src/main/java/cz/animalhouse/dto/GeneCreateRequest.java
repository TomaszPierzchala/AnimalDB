package cz.animalhouse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class GeneCreateRequest {

    @NotBlank
    @Size(max = 50)
    private String symbol;

    private String description;

    public GeneCreateRequest() {
    }

    public GeneCreateRequest(String symbol, String description) {
        this.symbol = symbol;
        this.description = description;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getDescription() {
        return description;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}