package cz.animalhouse.exception;

public class DuplicateStrainTransgenicLineNameException
        extends RuntimeException {

    public DuplicateStrainTransgenicLineNameException(String name) {
        super(
                "Strain/transgenic line with name '%s' already exists"
                        .formatted(name)
        );
    }
}
