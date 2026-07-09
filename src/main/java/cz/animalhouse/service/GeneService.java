package cz.animalhouse.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cz.animalhouse.dto.GeneCreateRequest;
import cz.animalhouse.dto.GeneResponse;
import cz.animalhouse.entity.Gene;
import cz.animalhouse.exception.DuplicateGeneSymbolException;
import cz.animalhouse.repository.GeneRepository;

@Service
public class GeneService {

    private final GeneRepository geneRepository;

    public GeneService(GeneRepository geneRepository) {
        this.geneRepository = geneRepository;
    }

    @Transactional(readOnly = true)
    public List<GeneResponse> findAll() {
        return geneRepository.findAll()
                .stream()
                .map(GeneResponse::fromEntity)
                .toList();
    }

    @Transactional
    public GeneResponse create(GeneCreateRequest request) {
        if (geneRepository.existsBySymbol(request.getSymbol())) {
            throw new DuplicateGeneSymbolException(request.getSymbol());
        }

        Gene gene = new Gene(
                request.getSymbol(),
                request.getDescription());

        Gene saved = geneRepository.save(gene);

        return GeneResponse.fromEntity(saved);
    }

    @Transactional
    public boolean delete(Long id) {
        if (!geneRepository.existsById(id)) {
            return false;
        }

        geneRepository.deleteById(id);
        return true;
    }
}
