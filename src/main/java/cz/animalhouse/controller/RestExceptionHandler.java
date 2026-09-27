package cz.animalhouse.controller;

import cz.animalhouse.exception.DuplicateGeneSymbolException;
import cz.animalhouse.exception.DuplicateMouseAnimalNumberException;
import cz.animalhouse.exception.DuplicateStrainCodeException;
import cz.animalhouse.exception.TransgenicLineGeneNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

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
    
    @ExceptionHandler(TransgenicLineGeneNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleTransgenicLineGeneNotFoundException(TransgenicLineGeneNotFoundException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    public record ErrorResponse(String message) {
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException exception) {

        var fieldError = exception.getBindingResult()
                .getFieldErrors()
                .getFirst();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", fieldError.getField() + ": " + fieldError.getDefaultMessage());
        response.put("rejectedValue", fieldError.getRejectedValue());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> handleNotFound(
            NoSuchElementException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }

    @ExceptionHandler(DuplicateMouseAnimalNumberException.class)
    public ResponseEntity<String> handleDuplicate(
            DuplicateMouseAnimalNumberException ex) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleInvalidArgument(
            IllegalArgumentException ex) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }
}
