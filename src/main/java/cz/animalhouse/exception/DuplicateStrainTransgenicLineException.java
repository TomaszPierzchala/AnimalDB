package cz.animalhouse.exception;

public class DuplicateStrainTransgenicLineException
        extends RuntimeException {

    public DuplicateStrainTransgenicLineException(Long id, String name) {
        super(
                "StrainTransgenicLine with strainId=%d and name=%s already exists"
                        .formatted(id, name)
        );
    }
}
