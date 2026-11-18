package cz.animalhouse.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cz.animalhouse.entity.StrainTransgenicLine;

public interface StrainTransgenicLineRepository extends JpaRepository<StrainTransgenicLine, Long> {

	boolean existsByName(String name);

	boolean existsByNameAndIdNot(String name, Long id);
}
