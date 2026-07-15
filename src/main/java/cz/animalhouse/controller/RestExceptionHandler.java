package cz.animalhouse.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import cz.animalhouse.exception.DuplicateGeneSymbolException;
import cz.animalhouse.exception.DuplicateStrainCodeException;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(DuplicateGeneSymbolException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDuplicateGeneSymbol(DuplicateGeneSymbolException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(DuplicateStrainCodeException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDuplicateStrainCode(DuplicateStrainCodeException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    public record ErrorResponse(String message) {
    }
}