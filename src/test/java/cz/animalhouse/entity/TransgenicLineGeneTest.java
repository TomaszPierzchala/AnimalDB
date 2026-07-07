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
class TransgenicLineGeneTest {

    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldCreateEmbeddedIdFromMappedEntities() {

        Strain strain = entityManager.persistFlushFind(
                new Strain("C57BL/6J", "Black 6"));

        TransgenicLine transgenicLine = entityManager.persistFlushFind(
                new TransgenicLine(strain, "OT-I"));

        Gene gene = entityManager.persistFlushFind(
                new Gene("OT1", "Ovalbumin-specific T-cell receptor"));

        TransgenicLineGene transgenicLineGene =
                new TransgenicLineGene(transgenicLine, gene);

        entityManager.persistAndFlush(transgenicLineGene);
        entityManager.clear();

        TransgenicLineGeneId id =
                new TransgenicLineGeneId(transgenicLine.getId(), gene.getId());

        TransgenicLineGene saved =
                entityManager.find(TransgenicLineGene.class, id);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isEqualTo(id);

        assertThat(saved.getTransgenicLine().getId())
                .isEqualTo(transgenicLine.getId());

        assertThat(saved.getGene().getId())
                .isEqualTo(gene.getId());

        assertThat(saved).isEqualTo(transgenicLineGene);
        assertThat(saved.hashCode()).isEqualTo(transgenicLineGene.hashCode());

        assertThat(saved.toString())
                .contains("TransgenicLineGene [EmbeddedId=")
                .contains("(transgenicLineId=" + transgenicLine.getId())
                .contains(", geneId=" + gene.getId());
    }

    @Test
    void shouldRejectNullTransgenicLineAssociation() {

        Gene gene = entityManager.persistFlushFind(
                new Gene("GFP", "Green fluorescent protein"));

        TransgenicLineGene transgenicLineGene =
                new TransgenicLineGene(null, gene);

        assertThatThrownBy(() -> entityManager.persistAndFlush(transgenicLineGene))
                .isInstanceOf(org.hibernate.id.IdentifierGenerationException.class)
                .hasMessageContaining("Could not assign id from null association 'transgenicLine'");
    }

    @Test
    void shouldRejectNullGeneAssociation() {

        Strain strain = entityManager.persistFlushFind(
                new Strain("C57BL/6J", "Black 6"));

        TransgenicLine transgenicLine = entityManager.persistFlushFind(
                new TransgenicLine(strain, "OT-I"));

        TransgenicLineGene transgenicLineGene =
                new TransgenicLineGene(transgenicLine, null);

        assertThatThrownBy(() -> entityManager.persistAndFlush(transgenicLineGene))
                .isInstanceOf(org.hibernate.id.IdentifierGenerationException.class)
                .hasMessageContaining("Could not assign id from null association 'gene'");
    }

    @Test
    void shouldRejectDuplicateTransgenicLineGene() {

        Strain strain = entityManager.persistFlushFind(
                new Strain("C57BL/6J", "Black 6"));

        TransgenicLine transgenicLine = entityManager.persistFlushFind(
                new TransgenicLine(strain, "OT-I"));

        Gene gene = entityManager.persistFlushFind(
                new Gene("OT1", "Ovalbumin-specific T-cell receptor"));

        TransgenicLineGene first =
                new TransgenicLineGene(transgenicLine, gene);

        entityManager.persistAndFlush(first);
        entityManager.clear();

        TransgenicLineGene duplicate =
                new TransgenicLineGene(transgenicLine, gene);

        assertThatThrownBy(() -> entityManager.persistAndFlush(duplicate))
                .hasRootCauseInstanceOf(org.postgresql.util.PSQLException.class)
                .hasMessageContaining("duplicate key value violates unique constraint");
    }
}