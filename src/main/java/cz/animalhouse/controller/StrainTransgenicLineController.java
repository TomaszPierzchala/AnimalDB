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

import cz.animalhouse.dto.StrainTransgenicLineRequest;
import cz.animalhouse.dto.StrainTransgenicLineResponse;
import cz.animalhouse.service.StrainTransgenicLineService;
import jakarta.validation.Valid;

@CrossOrigin(origins = "${app.cors.allowed-origin}")
@RestController
@RequestMapping("/api/transgenic-lines")
public class StrainTransgenicLineController {

    private final StrainTransgenicLineService transgenicLineService;

    public StrainTransgenicLineController(
            StrainTransgenicLineService transgenicLineService) {

        this.transgenicLineService = transgenicLineService;
    }

    @GetMapping
    public List<StrainTransgenicLineResponse> findAll() {
        return transgenicLineService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<StrainTransgenicLineResponse> findById(
            @PathVariable Long id) {

        return transgenicLineService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StrainTransgenicLineResponse create(
            @Valid @RequestBody StrainTransgenicLineRequest request) {

        return transgenicLineService.create(request);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StrainTransgenicLineResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody StrainTransgenicLineRequest request) {

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