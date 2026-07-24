package cz.animalhouse.exception;

import cz.animalhouse.entity.TransgenicLineGeneId;

public class TransgenicLineGeneNotFoundException extends RuntimeException {

    public TransgenicLineGeneNotFoundException(TransgenicLineGeneId id) {
        super("TransgenicLine_Gene assignment does not exist: " + 
        	  "transgenicLineId=%d, geneId=%d"
                    .formatted(
                            id.transgenicLineId(),
                            id.geneId()
                    )
             );
    }
}