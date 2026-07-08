package cz.animalhouse.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import cz.animalhouse.dto.GeneCreateRequest;
import cz.animalhouse.dto.GeneResponse;
import cz.animalhouse.service.GeneService;
import jakarta.validation.Valid;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/genes")
public class GeneController {

    private final GeneService geneService;

    public GeneController(GeneService geneService) {
        this.geneService = geneService;
    }

    @GetMapping
    public List<GeneResponse> getAllGenes() {
        return geneService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GeneResponse createGene(@Valid @RequestBody GeneCreateRequest request) {
        return geneService.create(request);
    }
}