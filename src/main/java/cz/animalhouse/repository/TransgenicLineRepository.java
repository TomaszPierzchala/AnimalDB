package cz.animalhouse.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cz.animalhouse.entity.TransgenicLine;

public interface TransgenicLineRepository extends JpaRepository<TransgenicLine, Long> {

	boolean existsByName(String name);

	boolean existsByNameAndIdNot(String name, Long id);
}
