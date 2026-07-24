package cz.animalhouse.dto;

import java.util.List;

public record TransgenicLineGeneRowResponse(
        Long transgenicLineId,
        String transgenicLineName,
        Long strainId,
        String strainCode,
        String strainName,
        List<GeneResponse> genes
) {
}