package cz.animalhouse.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cz.animalhouse.dto.TransgenicLineRequest;
import cz.animalhouse.dto.TransgenicLineResponse;
import cz.animalhouse.entity.Strain;
import cz.animalhouse.entity.TransgenicLine;
import cz.animalhouse.exception.DuplicateTransgenicLineNameException;
import cz.animalhouse.exception.StrainNotFoundException;
import cz.animalhouse.repository.StrainRepository;
import cz.animalhouse.repository.TransgenicLineRepository;

@Service
public class TransgenicLineService {

    private final TransgenicLineRepository transgenicLineRepository;
    private final StrainRepository strainRepository;

    public TransgenicLineService(
            TransgenicLineRepository transgenicLineRepository,
            StrainRepository strainRepository) {

        this.transgenicLineRepository = transgenicLineRepository;
        this.strainRepository = strainRepository;
    }

    @Transactional(readOnly = true)
    public List<TransgenicLineResponse> findAll() {
        return transgenicLineRepository.findAll()
                .stream()
                .map(TransgenicLineResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<TransgenicLineResponse> findById(Long id) {
        return transgenicLineRepository.findById(id)
                .map(TransgenicLineResponse::fromEntity);
    }

    @Transactional
    public TransgenicLineResponse create(
            TransgenicLineRequest request) {

        if (transgenicLineRepository.existsByName(request.name())) {
            throw new DuplicateTransgenicLineNameException(
                    request.name()
            );
        }

        Strain strain = findStrain(request.strainId());

        TransgenicLine transgenicLine = new TransgenicLine(
                strain,
                request.name()
        );

        TransgenicLine saved =
                transgenicLineRepository.save(transgenicLine);

        return TransgenicLineResponse.fromEntity(saved);
    }

    @Transactional
    public Optional<TransgenicLineResponse> update(
            Long id,
            TransgenicLineRequest request) {

        Optional<TransgenicLine> optionalTransgenicLine =
                transgenicLineRepository.findById(id);

        if (optionalTransgenicLine.isEmpty()) {
            return Optional.empty();
        }

        if (transgenicLineRepository.existsByNameAndIdNot(
                request.name(),
                id)) {

            throw new DuplicateTransgenicLineNameException(
                    request.name()
            );
        }

        Strain strain = findStrain(request.strainId());

        TransgenicLine transgenicLine =
                optionalTransgenicLine.get();

        transgenicLine.setStrain(strain);
        transgenicLine.setName(request.name());

        return Optional.of(
                TransgenicLineResponse.fromEntity(transgenicLine)
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
