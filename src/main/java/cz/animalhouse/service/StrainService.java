package cz.animalhouse.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cz.animalhouse.dto.StrainCreateRequest;
import cz.animalhouse.dto.StrainResponse;
import cz.animalhouse.entity.Strain;
import cz.animalhouse.exception.DuplicateStrainCodeException;
import cz.animalhouse.repository.StrainRepository;

@Service
public class StrainService {

    private final StrainRepository strainRepository;

    public StrainService(StrainRepository strainRepository) {
        this.strainRepository = strainRepository;
    }

    @Transactional(readOnly = true)
    public List<StrainResponse> findAll() {
        return strainRepository.findAll()
                .stream()
                .map(StrainResponse::fromEntity)
                .toList();
    }

    @Transactional
    public StrainResponse create(StrainCreateRequest request) {
        if (strainRepository.existsByCode(request.getCode())) {
            throw new DuplicateStrainCodeException(request.getCode());
        }

        Strain strain = new Strain(
                request.getCode(),
                request.getName());

        Strain saved = strainRepository.save(strain);

        return StrainResponse.fromEntity(saved);
    }

    @Transactional
    public boolean delete(Long id) {
        if (!strainRepository.existsById(id)) {
            return false;
        }

        strainRepository.deleteById(id);
        return true;
    }
    
    @Transactional
    public Optional<StrainResponse> update(
            Long id,
            StrainCreateRequest request) {

        Optional<Strain> optionalStrain = strainRepository.findById(id);

        if (optionalStrain.isEmpty()) {
            return Optional.empty();
        }

        if (strainRepository.existsByCodeAndIdNot(request.getCode(), id)) {
            throw new DuplicateStrainCodeException(request.getCode());
        }

        Strain strain = optionalStrain.get();

        strain.setCode(request.getCode());
        strain.setName(request.getName());

        return Optional.of(StrainResponse.fromEntity(strain));
    }
}
