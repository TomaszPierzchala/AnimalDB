package cz.animalhouse.exception;

import cz.animalhouse.entity.TransgenicLineGeneId;

public class TransgenicLineGeneNotFoundException extends RuntimeException {

    public TransgenicLineGeneNotFoundException(TransgenicLineGeneId id) {
        super("TransgenicLine_Gene : transgenicLineId=%d, geneId=%d assignment no longer exists.\n"
               .formatted(
                            id.transgenicLineId(),
                            id.geneId()
                    )
               + "Most probably was deleted by other user."
             );
    }
}