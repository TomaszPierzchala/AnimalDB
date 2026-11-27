package cz.animalhouse.repository;

import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import cz.animalhouse.entity.StrainTransgenicLine;

import java.util.List;

public interface StrainTransgenicLineRepository extends JpaRepository<StrainTransgenicLine, Long> {

    @Override
    @NonNull
    @EntityGraph(attributePaths = "strain")
    List<StrainTransgenicLine> findAll();

	boolean existsByStrainIdAndName(Long strainId, String name);


    boolean existsByStrainIdAndNameAndIdNot(
            Long strainId,
            String name,
            Long id
    );
}
