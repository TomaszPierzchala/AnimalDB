package cz.animalhouse.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cz.animalhouse.dto.GeneResponse;
import cz.animalhouse.dto.TransgenicLineGeneRequest;
import cz.animalhouse.dto.TransgenicLineGeneRowResponse;
import cz.animalhouse.entity.Gene;
import cz.animalhouse.entity.StrainTransgenicLine;
import cz.animalhouse.entity.TransgenicLineGene;
import cz.animalhouse.entity.TransgenicLineGeneId;
import cz.animalhouse.exception.DuplicateTransgenicLineGeneException;
import cz.animalhouse.exception.GeneNotFoundException;
import cz.animalhouse.exception.TransgenicLineGeneNotFoundException;
import cz.animalhouse.exception.TransgenicLineNotFoundException;
import cz.animalhouse.repository.GeneRepository;
import cz.animalhouse.repository.TransgenicLineGeneRepository;
import cz.animalhouse.repository.StrainTransgenicLineRepository;

@Service
public class TransgenicLineGeneService {

    private final TransgenicLineGeneRepository
            transgenicLineGeneRepository;

    private final StrainTransgenicLineRepository
            transgenicLineRepository;

    private final GeneRepository geneRepository;

    public TransgenicLineGeneService(
            TransgenicLineGeneRepository transgenicLineGeneRepository,
            StrainTransgenicLineRepository transgenicLineRepository,
            GeneRepository geneRepository) {

        this.transgenicLineGeneRepository =
                transgenicLineGeneRepository;

        this.transgenicLineRepository =
                transgenicLineRepository;

        this.geneRepository = geneRepository;
    }

    @Transactional(readOnly = true)
    public List<TransgenicLineGeneRowResponse> findAllGrouped() {
        List<TransgenicLineGene> assignments =
                transgenicLineGeneRepository.findAll();

        Map<Long, RowBuilder> rows = new LinkedHashMap<>();

        for (TransgenicLineGene assignment : assignments) {
            StrainTransgenicLine line =
                    assignment.getTransgenicLine();

            rows.computeIfAbsent(
                    line.getId(),
                    ignored -> new RowBuilder(line)
            ).genes().add(
                    GeneResponse.fromEntity(
                            assignment.getGene()
                    )
            );
        }

        return rows.values()
                .stream()
                .map(RowBuilder::toResponse)
                .toList();
    }

    @Transactional
    public TransgenicLineGeneRowResponse create(
            TransgenicLineGeneRequest request) {

        TransgenicLineGeneId id = new TransgenicLineGeneId(
                request.transgenicLineId(),
                request.geneId()
        );

        if (transgenicLineGeneRepository.existsById(id)) {
            throw new DuplicateTransgenicLineGeneException(
                    request.transgenicLineId(),
                    request.geneId()
            );
        }

        StrainTransgenicLine line =
                findTransgenicLine(request.transgenicLineId());

        Gene gene = findGene(request.geneId());

        transgenicLineGeneRepository.save(
                new TransgenicLineGene(line, gene)
        );

        return createSingleRowResponse(line, gene);
    }

    @Transactional
    public TransgenicLineGeneRowResponse update(
            Long currentTransgenicLineId,
            Long currentGeneId,
            TransgenicLineGeneRequest request) {

        TransgenicLineGeneId currentId =
                new TransgenicLineGeneId(
                        currentTransgenicLineId,
                        currentGeneId
                );

        TransgenicLineGene current =
                transgenicLineGeneRepository
                        .findById(currentId)
                        .orElseThrow(() ->
                                new TransgenicLineGeneNotFoundException(currentId)
                        );

        TransgenicLineGeneId newId =
                new TransgenicLineGeneId(
                        request.transgenicLineId(),
                        request.geneId()
                );

        if (
            !currentId.equals(newId) &&
            transgenicLineGeneRepository.existsById(newId)
        ) {
            throw new DuplicateTransgenicLineGeneException(
                    request.transgenicLineId(),
                    request.geneId()
            );
        }

        StrainTransgenicLine newLine =
                findTransgenicLine(request.transgenicLineId());

        Gene newGene = findGene(request.geneId());

        if (!currentId.equals(newId)) {
            transgenicLineGeneRepository.delete(current);

            transgenicLineGeneRepository.save(
                    new TransgenicLineGene(
                            newLine,
                            newGene
                    )
            );
        }

        return createSingleRowResponse(
                newLine,
                newGene
        );
    }

    @Transactional
    public boolean delete(
            Long transgenicLineId,
            Long geneId) {

        TransgenicLineGeneId id =
                new TransgenicLineGeneId(
                        transgenicLineId,
                        geneId
                );

        if (!transgenicLineGeneRepository.existsById(id)) {
            return false;
        }

        transgenicLineGeneRepository.deleteById(id);

        return true;
    }

    @Transactional
	public long deleteAllByTransgenicLineId(
	        Long transgenicLineId) {

	    return transgenicLineGeneRepository
	            .deleteByTransgenicLine_Id(
	                    transgenicLineId
	            );
	}

    private StrainTransgenicLine findTransgenicLine(Long id) {
        return transgenicLineRepository.findById(id)
                .orElseThrow(() ->
                        new TransgenicLineNotFoundException(id)
                );
    }

    private Gene findGene(Long id) {
        return geneRepository.findById(id)
                .orElseThrow(() ->
                        new GeneNotFoundException(id)
                );
    }

    private TransgenicLineGeneRowResponse createSingleRowResponse(
            StrainTransgenicLine line,
            Gene gene) {

        return new TransgenicLineGeneRowResponse(
                line.getId(),
                line.getName(),
                line.getStrain().getId(),
                line.getStrain().getCode(),
                line.getStrain().getName(),
                List.of(GeneResponse.fromEntity(gene))
        );
    }

    private record RowBuilder(
            StrainTransgenicLine line,
            List<GeneResponse> genes
    ) {

        private RowBuilder(StrainTransgenicLine line) {
            this(line, new java.util.ArrayList<>());
        }

        private TransgenicLineGeneRowResponse toResponse() {
            return new TransgenicLineGeneRowResponse(
                    line.getId(),
                    line.getName(),
                    line.getStrain().getId(),
                    line.getStrain().getCode(),
                    line.getStrain().getName(),
                    List.copyOf(genes)
            );
        }
    }
}