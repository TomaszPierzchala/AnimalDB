package cz.animalhouse.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cz.animalhouse.entity.Gene;

public interface GeneRepository extends JpaRepository<Gene, Long> {

	boolean existsBySymbol(String symbol);
}