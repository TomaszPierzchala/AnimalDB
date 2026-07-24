package cz.animalhouse.exception;

public class TransgenicLineNotFoundException extends RuntimeException {

    public TransgenicLineNotFoundException(Long id) {
        super("Transgenic Line with ID %d does not exist".formatted(id));
    }
}