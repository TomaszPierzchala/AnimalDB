package cz.animalhouse.exception;

public class GeneNotFoundException extends RuntimeException {

    public GeneNotFoundException(Long id) {
        super("Gene with ID %d does not exist".formatted(id));
    }
}