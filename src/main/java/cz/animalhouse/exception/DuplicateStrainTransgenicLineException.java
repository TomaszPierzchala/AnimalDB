package cz.animalhouse.exception;

public class DuplicateStrainTransgenicLineException
        extends RuntimeException {

    public DuplicateStrainTransgenicLineException(Long id, String name) {
        super(
                "Strain/transgenic line with strainId '%d' & name '%s' already exists"
                        .formatted(id, name)
        );
    }
}
