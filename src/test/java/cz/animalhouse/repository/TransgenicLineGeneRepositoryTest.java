package cz.animalhouse.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.postgresql.PostgreSQLContainer;

import cz.animalhouse.entity.Gene;
import cz.animalhouse.entity.Strain;
import cz.animalhouse.entity.TransgenicLine;
import cz.animalhouse.entity.TransgenicLineGene;
import cz.animalhouse.entity.TransgenicLineGeneId;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceUnitUtil;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class TransgenicLineGeneRepositoryTest {

    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17");

    @Autowired
    private TransgenicLineGeneRepository
            transgenicLineGeneRepository;

    @Autowired
    private TransgenicLineRepository
            transgenicLineRepository;

    @Autowired
    private StrainRepository strainRepository;

    @Autowired
    private GeneRepository geneRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldInsertTwoAssignmentsAndFindAll() {
        Strain strain = strainRepository.save(
                new Strain(
                        "C57BL6",
                        "C57BL/6"
                )
        );

        TransgenicLine transgenicLine =
                transgenicLineRepository.save(
                        new TransgenicLine(
                                strain,
                                "OT-I"
                        )
                );

        Gene gene1 = geneRepository.save(
                new Gene(
                        "OT1",
                        "Ovalbumin-specific T-cell receptor"
                )
        );

        Gene gene2 = geneRepository.save(
                new Gene(
                        "GFP",
                        "Green fluorescent protein"
                )
        );

        TransgenicLineGene assignment1 =
                new TransgenicLineGene(
                        transgenicLine,
                        gene1
                );

        TransgenicLineGene assignment2 =
                new TransgenicLineGene(
                        transgenicLine,
                        gene2
                );

        transgenicLineGeneRepository.saveAll(
                List.of(assignment1, assignment2)
        );

        transgenicLineGeneRepository.flush();
        entityManager.clear();

        List<TransgenicLineGene> assignments =
                transgenicLineGeneRepository.findAll();

        assertThat(assignments).hasSize(2);

        assertThat(assignments)
                .extracting(assignment ->
                        assignment.getTransgenicLine().getName())
                .containsOnly("OT-I");

        assertThat(assignments)
                .extracting(assignment ->
                        assignment.getTransgenicLine()
                                .getStrain()
                                .getCode())
                .containsOnly("C57BL6");

        assertThat(assignments)
                .extracting(assignment ->
                        assignment.getGene().getSymbol())
                .containsExactlyInAnyOrder("OT1", "GFP");
    }

    @Test
    void shouldCreateCompositeIds() {
        Strain strain = strainRepository.save(
                new Strain(
                        "C57BL6",
                        "C57BL/6"
                )
        );

        TransgenicLine transgenicLine =
                transgenicLineRepository.save(
                        new TransgenicLine(
                                strain,
                                "OT-I"
                        )
                );

        Gene gene = geneRepository.save(
                new Gene(
                        "GFP",
                        "Green fluorescent protein"
                )
        );

        TransgenicLineGene assignment =
                new TransgenicLineGene(
                        transgenicLine,
                        gene
                );

        transgenicLineGeneRepository.saveAndFlush(assignment);

        entityManager.clear();

        TransgenicLineGeneId expectedId =
                new TransgenicLineGeneId(
                        transgenicLine.getId(),
                        gene.getId()
                );

        TransgenicLineGene saved =
                transgenicLineGeneRepository
                        .findById(expectedId)
                        .orElseThrow();

        assertThat(saved.getId()).isEqualTo(expectedId);

        assertThat(saved.getId().transgenicLineId())
                .isEqualTo(transgenicLine.getId());

        assertThat(saved.getId().geneId())
                .isEqualTo(gene.getId());
    }

    @Test
    void shouldLoadRelationsUsingEntityGraph() {
        Strain strain = strainRepository.save(
                new Strain(
                        "C57BL6",
                        "C57BL/6"
                )
        );

        TransgenicLine transgenicLine =
                transgenicLineRepository.save(
                        new TransgenicLine(
                                strain,
                                "OT-I"
                        )
                );

        Gene gene = geneRepository.save(
                new Gene(
                        "GFP",
                        "Green fluorescent protein"
                )
        );

        transgenicLineGeneRepository.saveAndFlush(
                new TransgenicLineGene(
                        transgenicLine,
                        gene
                )
        );

        entityManager.clear();

        List<TransgenicLineGene> assignments =
                transgenicLineGeneRepository.findAll();

        assertThat(assignments).hasSize(1);

        TransgenicLineGene assignment =
                assignments.getFirst();

        PersistenceUnitUtil persistenceUnitUtil =
                entityManager
                        .getEntityManagerFactory()
                        .getPersistenceUnitUtil();

        assertThat(
                persistenceUnitUtil.isLoaded(
                        assignment,
                        "transgenicLine"
                )
        ).isTrue();

        assertThat(
                persistenceUnitUtil.isLoaded(
                        assignment.getTransgenicLine(),
                        "strain"
                )
        ).isTrue();

        assertThat(
                persistenceUnitUtil.isLoaded(
                        assignment,
                        "gene"
                )
        ).isTrue();
    }

    @Test
    void shouldDeleteOneAssignmentAndKeepTheOther() {
        Strain strain = strainRepository.save(
                new Strain(
                        "C57BL6",
                        "C57BL/6"
                )
        );

        TransgenicLine transgenicLine =
                transgenicLineRepository.save(
                        new TransgenicLine(
                                strain,
                                "OT-I"
                        )
                );

        Gene gene1 = geneRepository.save(
                new Gene(
                        "OT1",
                        "Ovalbumin-specific T-cell receptor"
                )
        );

        Gene gene2 = geneRepository.save(
                new Gene(
                        "GFP",
                        "Green fluorescent protein"
                )
        );

        TransgenicLineGene assignment1 =
                new TransgenicLineGene(
                        transgenicLine,
                        gene1
                );

        TransgenicLineGene assignment2 =
                new TransgenicLineGene(
                        transgenicLine,
                        gene2
                );

        transgenicLineGeneRepository.saveAll(
                List.of(assignment1, assignment2)
        );

        transgenicLineGeneRepository.flush();
        entityManager.clear();

        TransgenicLineGeneId idToDelete =
                new TransgenicLineGeneId(
                        transgenicLine.getId(),
                        gene1.getId()
                );

        transgenicLineGeneRepository.deleteById(idToDelete);

        transgenicLineGeneRepository.flush();
        entityManager.clear();

        List<TransgenicLineGene> remaining =
                transgenicLineGeneRepository.findAll();

        assertThat(remaining).hasSize(1);

        assertThat(remaining.getFirst().getGene().getSymbol())
                .isEqualTo("GFP");
    }

    @Test
    void shouldDeleteAllAssignmentsForGivenTransgenicLine() {
        Strain strain = strainRepository.save(
                new Strain(
                        "C57BL6",
                        "C57BL/6"
                )
        );

        TransgenicLine line1 =
                transgenicLineRepository.save(
                        new TransgenicLine(
                                strain,
                                "Line 1"
                        )
                );

        TransgenicLine line2 =
                transgenicLineRepository.save(
                        new TransgenicLine(
                                strain,
                                "Line 2"
                        )
                );

        Gene gene1 = geneRepository.save(
                new Gene(
                        "GENE1",
                        "Gene 1"
                )
        );

        Gene gene2 = geneRepository.save(
                new Gene(
                        "GENE2",
                        "Gene 2"
                )
        );

        Gene gene3 = geneRepository.save(
                new Gene(
                        "GENE3",
                        "Gene 3"
                )
        );

        transgenicLineGeneRepository.saveAll(
                List.of(
                        new TransgenicLineGene(
                                line1,
                                gene1
                        ),
                        new TransgenicLineGene(
                                line1,
                                gene2
                        ),
                        new TransgenicLineGene(
                                line2,
                                gene3
                        )
                )
        );

        entityManager.flush();
        entityManager.clear();

        long deletedCount =
                transgenicLineGeneRepository
                        .deleteByTransgenicLine_Id(
                                line1.getId()
                        );

        entityManager.flush();
        entityManager.clear();

        assertThat(deletedCount).isEqualTo(2);

        assertThat(transgenicLineGeneRepository.findAll())
                .hasSize(1)
                .allSatisfy(assignment ->
                        assertThat(
                                assignment
                                        .getTransgenicLine()
                                        .getId()
                        ).isEqualTo(line2.getId())
                );
    }

    @Test
    void shouldReturnZeroWhenTransgenicLineHasNoAssignments() {
        long deletedCount =
                transgenicLineGeneRepository
                        .deleteByTransgenicLine_Id(
                                999L
                        );

        assertThat(deletedCount).isZero();
    }
}