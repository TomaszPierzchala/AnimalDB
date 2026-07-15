package cz.animalhouse.exception;

public class DuplicateStrainCodeException extends RuntimeException {

    public DuplicateStrainCodeException(String code) {
        super("Straib with code \"" + code + "\" already exists");
    }
}
