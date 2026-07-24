package cz.animalhouse.exception;

public class DuplicateTransgenicLineGeneException
        extends RuntimeException {

    public DuplicateTransgenicLineGeneException(
            Long transgenicLineId,
            Long geneId) {

        super(
        		"Gene with ID %d is already assigned to transgenic line with ID %d"
                        .formatted(geneId, transgenicLineId)
        );
    }
}