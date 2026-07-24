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

import cz.animalhouse.dto.TransgenicLineGeneRequest;
import cz.animalhouse.dto.TransgenicLineGeneRowResponse;
import cz.animalhouse.service.TransgenicLineGeneService;
import jakarta.validation.Valid;

@CrossOrigin(origins = "${app.cors.allowed-origin}")
@RestController
@RequestMapping("/api/transgenic-line-genes")
public class TransgenicLineGeneController {

    private final TransgenicLineGeneService service;

    public TransgenicLineGeneController(
            TransgenicLineGeneService service) {

        this.service = service;
    }

    @GetMapping
    public List<TransgenicLineGeneRowResponse> findAll() {
        return service.findAllGrouped();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransgenicLineGeneRowResponse create(
            @Valid
            @RequestBody
            TransgenicLineGeneRequest request) {

        return service.create(request);
    }

    @PutMapping("/{transgenicLineId}/{geneId}")
    public TransgenicLineGeneRowResponse update(
            @PathVariable Long transgenicLineId,
            @PathVariable Long geneId,
            @Valid
            @RequestBody
            TransgenicLineGeneRequest request) {

        return service.update(
                transgenicLineId,
                geneId,
                request
        );
    }

    @DeleteMapping("/{transgenicLineId}/{geneId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long transgenicLineId,
            @PathVariable Long geneId) {

        boolean deleted = service.delete(
                transgenicLineId,
                geneId
        );

        return deleted
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}