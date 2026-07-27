package cz.animalhouse.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import cz.animalhouse.entity.TransgenicLineGene;
import cz.animalhouse.entity.TransgenicLineGeneId;

public interface TransgenicLineGeneRepository
        extends JpaRepository<
                TransgenicLineGene,
                TransgenicLineGeneId> {

    @Override
    @EntityGraph(attributePaths = {
            "transgenicLine",
            "transgenicLine.strain",
            "gene"
    })
    List<TransgenicLineGene> findAll();

    long deleteByTransgenicLine_Id(
            Long transgenicLineId
    );
}