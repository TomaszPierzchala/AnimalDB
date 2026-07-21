package cz.animalhouse.exception;

public class StrainNotFoundException extends RuntimeException {

    public StrainNotFoundException(Long id) {
        super(
                "Strain with ID %d does not exist"
                        .formatted(id)
        );
    }
}
