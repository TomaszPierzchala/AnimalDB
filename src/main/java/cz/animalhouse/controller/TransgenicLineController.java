package cz.animalhouse.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import cz.animalhouse.dto.TransgenicLineRequest;
import cz.animalhouse.dto.TransgenicLineResponse;
import cz.animalhouse.service.TransgenicLineService;
import jakarta.validation.Valid;

@CrossOrigin(origins = "${app.cors.allowed-origin}")
@RestController
@RequestMapping("/api/transgenic-lines")
public class TransgenicLineController {

    private final TransgenicLineService transgenicLineService;

    public TransgenicLineController(
            TransgenicLineService transgenicLineService) {

        this.transgenicLineService = transgenicLineService;
    }

    @GetMapping
    public List<TransgenicLineResponse> findAll() {
        return transgenicLineService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransgenicLineResponse> findById(
            @PathVariable Long id) {

        return transgenicLineService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransgenicLineResponse create(
            @Valid @RequestBody TransgenicLineRequest request) {

        return transgenicLineService.create(request);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransgenicLineResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody TransgenicLineRequest request) {

        return transgenicLineService.update(id, request)
                .map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id) {

        if (!transgenicLineService.delete(id)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }
}