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

import cz.animalhouse.dto.StrainCreateRequest;
import cz.animalhouse.dto.StrainResponse;
import cz.animalhouse.service.StrainService;
import jakarta.validation.Valid;

@CrossOrigin(origins = "${app.cors.allowed-origin}")
@RestController
@RequestMapping("/api/strain")
public class StrainController {

    private final StrainService strainService;

    public StrainController(StrainService strainService) {
        this.strainService = strainService;
    }

    @GetMapping
    public List<StrainResponse> getAllStrains() {
        return strainService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StrainResponse createStrain(@Valid @RequestBody StrainCreateRequest request) {
        return strainService.create(request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStrain(@PathVariable Long id) {
        boolean deleted = strainService.delete(id);

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<StrainResponse> updateStrain(
            @PathVariable Long id,
            @Valid @RequestBody StrainCreateRequest request) {

        return strainService.update(id, request)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
