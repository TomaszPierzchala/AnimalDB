package cz.animalhouse.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.postgresql.PostgreSQLContainer;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class GeneTest {

    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17");

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldPersistAndLoadGene() {

        Gene gene = new Gene(
                "OT1",
                "Ovalbumin-specific T-cell receptor");

        entityManager.persistAndFlush(gene);
        entityManager.clear();

        Gene saved = entityManager.find(Gene.class, gene.getId());

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();

        assertThat(saved.getSymbol()).isEqualTo("OT1");
        assertThat(saved.getDescription())
                .isEqualTo("Ovalbumin-specific T-cell receptor");

        assertThat(saved).isEqualTo(gene);
        assertThat(saved.hashCode()).isEqualTo(gene.hashCode());

        assertThat(saved.toString())
                .contains("Gene [id=")
                .contains("OT1")
                .contains("Ovalbumin-specific T-cell receptor");
    }

    @Test
    void shouldUpdateGene() {

        Gene gene = new Gene(
                "Cre",
                "Cre recombinase");

        entityManager.persistAndFlush(gene);

        gene.setDescription("Improved Cre recombinase");

        entityManager.persistAndFlush(gene);
        entityManager.clear();

        Gene saved = entityManager.find(Gene.class, gene.getId());

        assertThat(saved.getDescription())
                .isEqualTo("Improved Cre recombinase");
    }

    @Test
    void shouldAllowNullDescription() {

        Gene gene = new Gene(
                "RFP",
                null);

        entityManager.persistAndFlush(gene);
        entityManager.clear();

        Gene saved = entityManager.find(Gene.class, gene.getId());

        assertThat(saved.getDescription()).isNull();
    }

    @Test
    void shouldRejectDuplicateSymbol() {

        entityManager.persistAndFlush(
                new Gene("GFP", "Green fluorescent protein"));

        Gene duplicate =
                new Gene("GFP", "Another description");

        assertThatThrownBy(() ->
                entityManager.persistAndFlush(duplicate))
                .hasRootCauseInstanceOf(org.postgresql.util.PSQLException.class)
                .hasMessageContaining("duplicate key value violates unique constraint");
    }

    @Test
    void shouldRejectNullSymbol() {

        Gene gene =
                new Gene(null, "Description");

        assertThatThrownBy(() ->
                entityManager.persistAndFlush(gene))
                .hasRootCauseInstanceOf(org.postgresql.util.PSQLException.class)
                .hasMessageContaining("null value in column \"symbol\"");
    }
}