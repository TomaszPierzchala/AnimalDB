package cz.animalhouse.exception;

public class DuplicateGeneSymbolException extends RuntimeException {

    public DuplicateGeneSymbolException(String symbol) {
        super("Gene with symbol \"" + symbol + "\" already exists");
    }
}
