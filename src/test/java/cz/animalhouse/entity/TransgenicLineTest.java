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
class TransgenicLineTest {

    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17");

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldPersistAndLoadTransgenicLine() {

        Strain strain =
                new Strain("C57BL/6J", "Black 6");

        entityManager.persist(strain);

        TransgenicLine line =
                new TransgenicLine(strain, "OT-I");

        entityManager.persistAndFlush(line);
        entityManager.clear();

        TransgenicLine saved =
                entityManager.find(TransgenicLine.class, line.getId());

        assertThat(saved).isNotNull();

        assertThat(saved.getName()).isEqualTo("OT-I");

        assertThat(saved.getStrain().getCode())
                .isEqualTo("C57BL/6J");

        assertThat(saved).isEqualTo(line);

        assertThat(saved.hashCode())
                .isEqualTo(line.hashCode());

        assertThat(saved.toString())
                .contains("OT-I");
    }

    @Test
    void shouldUpdateName() {

        Strain strain =
                new Strain("FVB", "Friend Virus B");

        entityManager.persist(strain);

        TransgenicLine line =
                new TransgenicLine(strain, "Old name");

        entityManager.persistAndFlush(line);

        line.setName("New name");

        entityManager.persistAndFlush(line);
        entityManager.clear();

        TransgenicLine saved =
                entityManager.find(TransgenicLine.class, line.getId());

        assertThat(saved.getName())
                .isEqualTo("New name");
    }
    
    @Test
    void shouldRejectNullStrain() {

        TransgenicLine line =
                new TransgenicLine(null, "OT-I");

        assertThatThrownBy(() -> entityManager.persistAndFlush(line))
                .hasRootCauseInstanceOf(org.postgresql.util.PSQLException.class)
                .hasMessageContaining("ERROR: null value in column \"strain_id\"");
    }
    
    @Test
    void shouldRejectDuplicateName() {

        Strain strain =
                entityManager.persistFlushFind(new Strain("C57BL/6J", "Black 6"));

        entityManager.persistAndFlush(
                new TransgenicLine(strain, "OT-I"));

        TransgenicLine duplicate =
                new TransgenicLine(strain, "OT-I");

        assertThatThrownBy(() -> entityManager.persistAndFlush(duplicate))
                .hasRootCauseInstanceOf(org.postgresql.util.PSQLException.class)
                .hasMessageContaining("ERROR: duplicate key value violates unique constraint");
    }
    
    @Test
    void shouldRejectNullName() {

        Strain strain =
                entityManager.persistFlushFind(new Strain("BALB", "BALB/c"));

        TransgenicLine line =
                new TransgenicLine(strain, null);

        assertThatThrownBy(() -> entityManager.persistAndFlush(line))
                .hasRootCauseInstanceOf(org.postgresql.util.PSQLException.class)
                .hasMessageContaining("ERROR: null value in column \"name\"");
    }
}