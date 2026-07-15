package cz.animalhouse.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cz.animalhouse.entity.Strain;

public interface StrainRepository extends JpaRepository<Strain, Long> {

	boolean existsByCode(String code);

	boolean existsByCodeAndIdNot(String code, Long id);
}