package cz.animalhouse.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.postgresql.PostgreSQLContainer;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class StrainTransgenicLineTest {

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

        StrainTransgenicLine line =
                new StrainTransgenicLine(strain, "OT-I");

        entityManager.persistAndFlush(line);
        entityManager.clear();

        StrainTransgenicLine saved =
                entityManager.find(StrainTransgenicLine.class, line.getId());

        assertThat(saved).isNotNull();

        assertThat(saved.getName()).isEqualTo("OT-I");

        assertThat(saved.getStrain().getCode())
                .isEqualTo("C57BL/6J");

        assertThat(saved).isEqualTo(line);

        assertThat(saved.hashCode())
                .isEqualTo(line.hashCode());

        assertThat(saved.toString())
                .contains("StrainTransgenicLine [id=")
                .contains("OT-I")
                .contains("strain=Strain [id=").contains("code=C57BL/6J, name=Black 6");
    }

    @Test
    void shouldUpdateName() {

        Strain strain =
                new Strain("FVB", "Friend Virus B");

        entityManager.persist(strain);

        StrainTransgenicLine line =
                new StrainTransgenicLine(strain, "Old name");

        entityManager.persistAndFlush(line);

        line.setName("New name");

        entityManager.persistAndFlush(line);
        entityManager.clear();

        StrainTransgenicLine saved =
                entityManager.find(StrainTransgenicLine.class, line.getId());

        assert saved != null;
        assertThat(saved.getName())
                .isEqualTo("New name");
    }
    
    @Test
    void shouldRejectNullStrain() {

        StrainTransgenicLine line =
                new StrainTransgenicLine(null, "OT-I");

        assertThatThrownBy(() -> entityManager.persistAndFlush(line))
                .hasRootCauseInstanceOf(org.postgresql.util.PSQLException.class)
                .hasMessageContaining("ERROR: null value in column \"strain_id\"");
    }
    
    @Test
    void shouldAllowNullName() {

        Strain strain =
                entityManager.persistFlushFind(new Strain("BALB", "BALB/c"));

        StrainTransgenicLine line =
                new StrainTransgenicLine(strain, null);

        StrainTransgenicLine saved =
                entityManager.persistFlushFind(line);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isNull();
    }

    @Test
    void shouldRejectWhiteCharsStringOnlyName() {
        Strain strain =
                entityManager.persistFlushFind(new Strain("C57BL/6J", "Black 6"));

        final String WHITE_CHARS_ONLY = "       ";
        StrainTransgenicLine line =
                new StrainTransgenicLine(strain, WHITE_CHARS_ONLY);

        assertThatThrownBy(() -> entityManager.persistAndFlush(line))
                .hasRootCauseInstanceOf(org.postgresql.util.PSQLException.class)
                .hasMessageContaining("ERROR: new row for relation \"strain_transgenic_line\" violates check constraint \"chk_strain_transgenic_line_name\"");
    }

    @Test
    void shouldEnforceUniqueNameWithinStrain() {
        Strain strain1 = entityManager.persistFlushFind(
                new Strain("C57BL/6J", "Black 6"));

        Strain strain2 = entityManager.persistFlushFind(
                new Strain("BALB", "BALB/c"));

        // Same name within different strains is allowed
        StrainTransgenicLine line1 = entityManager.persistFlushFind(
                new StrainTransgenicLine(strain1, "OT-I"));

        StrainTransgenicLine line2 = entityManager.persistFlushFind(
                new StrainTransgenicLine(strain2, "OT-I"));

        assertThat(line1.getId()).isNotNull();
        assertThat(line2.getId()).isNotNull();
        assertThat(line1.getId()).isNotEqualTo(line2.getId());

        // Same name within the same strain is forbidden
        StrainTransgenicLine duplicate =
                new StrainTransgenicLine(strain1, "OT-I");

        assertThatThrownBy(() -> entityManager.persistAndFlush(duplicate))
                .hasRootCauseInstanceOf(org.postgresql.util.PSQLException.class)
                .hasMessageContaining(
                        "ERROR: duplicate key value violates unique constraint \"uq_strain_transgenic_line\"");
    }

    @Test
    void shouldEnforceUniqueNullNameWithinStrain() {
        Strain strain1 = entityManager.persistFlushFind(
                new Strain("C57BL/6J", "Black 6"));

        Strain strain2 = entityManager.persistFlushFind(
                new Strain("BALB", "BALB/c"));

        // NULL name for different strains is allowed
        StrainTransgenicLine line1 = entityManager.persistFlushFind(
                new StrainTransgenicLine(strain1, null));

        StrainTransgenicLine line2 = entityManager.persistFlushFind(
                new StrainTransgenicLine(strain2, null));

        assertThat(line1.getId()).isNotNull();
        assertThat(line2.getId()).isNotNull();
        assertThat(line1.getId()).isNotEqualTo(line2.getId());

        // Duplicate NULL name within the same strain is forbidden
        StrainTransgenicLine duplicate =
                new StrainTransgenicLine(strain1, null);

        assertThatThrownBy(() -> entityManager.persistAndFlush(duplicate))
                .hasRootCauseInstanceOf(org.postgresql.util.PSQLException.class)
                .hasMessageContaining(
                        "ERROR: duplicate key value violates unique constraint \"uq_strain_transgenic_line\"");
    }
}