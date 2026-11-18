package cz.animalhouse.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cz.animalhouse.dto.StrainTransgenicLineRequest;
import cz.animalhouse.dto.StrainTransgenicLineResponse;
import cz.animalhouse.entity.Strain;
import cz.animalhouse.entity.StrainTransgenicLine;
import cz.animalhouse.exception.DuplicateStrainTransgenicLineNameException;
import cz.animalhouse.exception.StrainNotFoundException;
import cz.animalhouse.repository.StrainRepository;
import cz.animalhouse.repository.StrainTransgenicLineRepository;

@Service
public class StrainTransgenicLineService {

    private final StrainTransgenicLineRepository transgenicLineRepository;
    private final StrainRepository strainRepository;

    public StrainTransgenicLineService(
            StrainTransgenicLineRepository transgenicLineRepository,
            StrainRepository strainRepository) {

        this.transgenicLineRepository = transgenicLineRepository;
        this.strainRepository = strainRepository;
    }

    @Transactional(readOnly = true)
    public List<StrainTransgenicLineResponse> findAll() {
        return transgenicLineRepository.findAll()
                .stream()
                .map(StrainTransgenicLineResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<StrainTransgenicLineResponse> findById(Long id) {
        return transgenicLineRepository.findById(id)
                .map(StrainTransgenicLineResponse::fromEntity);
    }

    @Transactional
    public StrainTransgenicLineResponse create(
            StrainTransgenicLineRequest request) {

        if (transgenicLineRepository.existsByName(request.name())) {
            throw new DuplicateStrainTransgenicLineNameException(
                    request.name()
            );
        }

        Strain strain = findStrain(request.strainId());

        StrainTransgenicLine transgenicLine = new StrainTransgenicLine(
                strain,
                request.name()
        );

        StrainTransgenicLine saved =
                transgenicLineRepository.save(transgenicLine);

        return StrainTransgenicLineResponse.fromEntity(saved);
    }

    @Transactional
    public Optional<StrainTransgenicLineResponse> update(
            Long id,
            StrainTransgenicLineRequest request) {

        Optional<StrainTransgenicLine> optionalStrainTransgenicLine =
                transgenicLineRepository.findById(id);

        if (optionalStrainTransgenicLine.isEmpty()) {
            return Optional.empty();
        }

        if (transgenicLineRepository.existsByNameAndIdNot(
                request.name(),
                id)) {

            throw new DuplicateStrainTransgenicLineNameException(
                    request.name()
            );
        }

        Strain strain = findStrain(request.strainId());

        StrainTransgenicLine transgenicLine =
                optionalStrainTransgenicLine.get();

        transgenicLine.setStrain(strain);
        transgenicLine.setName(request.name());

        return Optional.of(
                StrainTransgenicLineResponse.fromEntity(transgenicLine)
        );
    }

    @Transactional
    public boolean delete(Long id) {
        if (!transgenicLineRepository.existsById(id)) {
            return false;
        }

        transgenicLineRepository.deleteById(id);

        return true;
    }

    private Strain findStrain(Long strainId) {
        return strainRepository.findById(strainId)
                .orElseThrow(() ->
                        new StrainNotFoundException(strainId)
                );
    }
}
