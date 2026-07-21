package cz.animalhouse.exception;

public class DuplicateTransgenicLineNameException
        extends RuntimeException {

    public DuplicateTransgenicLineNameException(String name) {
        super(
                "Transgenic line with name '%s' already exists"
                        .formatted(name)
        );
    }
}
