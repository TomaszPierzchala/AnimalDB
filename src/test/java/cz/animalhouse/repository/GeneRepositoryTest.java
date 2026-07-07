package cz.animalhouse.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.postgresql.PostgreSQLContainer;

import cz.animalhouse.entity.Gene;
import jakarta.persistence.EntityManager;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class GeneRepositoryTest {

    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

    @Autowired
    private GeneRepository geneRepository;
    
    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldInsertTwoGenesAndFindAll() {

        Gene gene1 = new Gene(
                "OT1",
                "Ovalbumin-specific T-cell receptor");

        Gene gene2 = new Gene(
                "GFP",
                "Green fluorescent protein");

        geneRepository.saveAll(List.of(gene1, gene2));

        List<Gene> genes = geneRepository.findAll();

        assertThat(genes).hasSize(2);

        assertThat(genes)
                .extracting(Gene::getSymbol)
                .containsExactlyInAnyOrder("OT1", "GFP");

        assertThat(genes)
                .extracting(Gene::getDescription)
                .containsExactlyInAnyOrder(
                        "Ovalbumin-specific T-cell receptor",
                        "Green fluorescent protein");

        assertThat(gene1.getId()).isNotNull();
        assertThat(gene2.getId()).isNotNull();
    }
    
    @Test
    void shouldUpdateGeneAndFindAll() {

        Gene gene = new Gene(
                "OT1",
                "Ovalbumin-specific T-cell receptor");

        geneRepository.save(gene);

        gene.setSymbol("GFP");
        gene.setDescription("Green fluorescent protein");

        geneRepository.flush();
        entityManager.clear();

        List<Gene> genes = geneRepository.findAll();

        assertThat(genes).hasSize(1);

        Gene saved = genes.get(0);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getSymbol()).isEqualTo("GFP");
        assertThat(saved.getDescription()).isEqualTo("Green fluorescent protein");
    }
    
    @Test
    void shouldInsertTwoGenesDeleteOneAndFindRemaining() {

        Gene gene1 = new Gene(
                "OT1",
                "Ovalbumin-specific T-cell receptor");

        Gene gene2 = new Gene(
                "GFP",
                "Green fluorescent protein");

        geneRepository.save(gene1);
        geneRepository.save(gene2);

        geneRepository.flush();
        entityManager.clear();

        List<Gene> genesAfterInsert = geneRepository.findAll();

        assertThat(genesAfterInsert).hasSize(2);
        assertThat(genesAfterInsert)
                .extracting(Gene::getSymbol)
                .containsExactlyInAnyOrder("OT1", "GFP");

        Gene geneToDelete = genesAfterInsert.stream()
                .filter(gene -> "OT1".equals(gene.getSymbol()))
                .findFirst()
                .orElseThrow();

        geneRepository.delete(geneToDelete);

        geneRepository.flush();
        entityManager.clear();

        List<Gene> genesAfterDelete = geneRepository.findAll();

        assertThat(genesAfterDelete).hasSize(1);

        Gene remainingGene = genesAfterDelete.get(0);

        assertThat(remainingGene.getSymbol()).isEqualTo("GFP");
        assertThat(remainingGene.getDescription())
                .isEqualTo("Green fluorescent protein");
    }
}