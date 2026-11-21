package cz.animalhouse.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cz.animalhouse.entity.StrainTransgenicLine;

public interface StrainTransgenicLineRepository extends JpaRepository<StrainTransgenicLine, Long> {

	boolean existsByStrainIdAndName(Long strainId, String name);


    boolean existsByStrainIdAndNameAndIdNot(
            Long strainId,
            String name,
            Long id
    );
}
