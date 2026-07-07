package cz.animalhouse.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.postgresql.PostgreSQLContainer;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MouseTest {

    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldPersistAndLoadMouse() {

        Strain strain = entityManager.persistFlushFind(
                new Strain("C57BL/6J", "Black 6"));

        Person director = entityManager.persistFlushFind(
                new Person("Director", "Procedure director"));

        LabProcedure procedure = entityManager.persistFlushFind(
                new LabProcedure(
                        "P-32-2026",
                        "Test procedure",
                        "Test description",
                        director,
                        LocalDate.of(2026, 1, 1),
                        null));

        TransgenicLine transgenicLine = entityManager.persistFlushFind(
                new TransgenicLine(strain, "OT-I"));

        Mouse mouse = new Mouse(
                1001,
                Mouse.Sex.M,
                strain,
                transgenicLine,
                procedure,
                null,
                null,
                LocalDate.of(2026, 1, 10),
                null,
                "Room A",
                "Rack 1",
                "Cage 12",
                "Charles University",
                "Healthy mouse");

        entityManager.persistAndFlush(mouse);
        entityManager.clear();

        Mouse saved = entityManager.find(Mouse.class, mouse.getId());

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();

        assertThat(saved.getAnimalNumber()).isEqualTo(1001);
        assertThat(saved.getSex()).isEqualTo(Mouse.Sex.M);

        assertThat(saved.getStrain().getId()).isEqualTo(strain.getId());
        assertThat(saved.getTransgenicLine().getId()).isEqualTo(transgenicLine.getId());
        assertThat(saved.getLabProcedure().getId()).isEqualTo(procedure.getId());

        assertThat(saved.getBirthDate()).isEqualTo(LocalDate.of(2026, 1, 10));
        assertThat(saved.getDeathDate()).isNull();

        assertThat(saved.getRoom()).isEqualTo("Room A");
        assertThat(saved.getRack()).isEqualTo("Rack 1");
        assertThat(saved.getCage()).isEqualTo("Cage 12");
        assertThat(saved.getOrigin()).isEqualTo("Charles University");
        assertThat(saved.getNote()).isEqualTo("Healthy mouse");

        assertThat(saved).isEqualTo(mouse);
        assertThat(saved.hashCode()).isEqualTo(mouse.hashCode());

        assertThat(saved.toString())
                .contains("animalNumber=1001")
                .contains("sex=M");
    }

    @Test
    void shouldPersistMouseWithParents() {

        Strain strain = entityManager.persistFlushFind(
                new Strain("BALB", "BALB/c"));

        Mouse mother = entityManager.persistFlushFind(
                new Mouse(
                        2001,
                        Mouse.Sex.F,
                        strain,
                        null,
                        null,
                        null,
                        null,
                        LocalDate.of(2025, 1, 1),
                        null,
                        "Room A",
                        "Rack 1",
                        "Cage 1",
                        null,
                        "Mother mouse"));

        Mouse father = entityManager.persistFlushFind(
                new Mouse(
                        2002,
                        Mouse.Sex.M,
                        strain,
                        null,
                        null,
                        null,
                        null,
                        LocalDate.of(2025, 1, 2),
                        null,
                        "Room A",
                        "Rack 1",
                        "Cage 1",
                        null,
                        "Father mouse"));

        Mouse child = new Mouse(
                2003,
                Mouse.Sex.F,
                strain,
                null,
                null,
                mother,
                father,
                LocalDate.of(2026, 1, 10),
                null,
                "Room B",
                "Rack 2",
                "Cage 3",
                null,
                "Child mouse");

        entityManager.persistAndFlush(child);
        entityManager.clear();

        Mouse saved = entityManager.find(Mouse.class, child.getId());

        assertThat(saved).isNotNull();
        assertThat(saved.getMother().getId()).isEqualTo(mother.getId());
        assertThat(saved.getFather().getId()).isEqualTo(father.getId());
        assertThat(saved.getMother().getAnimalNumber()).isEqualTo(2001);
        assertThat(saved.getFather().getAnimalNumber()).isEqualTo(2002);
    }

    @Test
    void shouldAllowOptionalRelationsAndOptionalTextFieldsToBeNull() {

        Strain strain = entityManager.persistFlushFind(
                new Strain("FVB", "Friend Virus B"));

        Mouse mouse = new Mouse(
                3001,
                Mouse.Sex.F,
                strain,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        entityManager.persistAndFlush(mouse);
        entityManager.clear();

        Mouse saved = entityManager.find(Mouse.class, mouse.getId());

        assertThat(saved).isNotNull();

        assertThat(saved.getTransgenicLine()).isNull();
        assertThat(saved.getLabProcedure()).isNull();
        assertThat(saved.getMother()).isNull();
        assertThat(saved.getFather()).isNull();

        assertThat(saved.getBirthDate()).isNull();
        assertThat(saved.getDeathDate()).isNull();

        assertThat(saved.getRoom()).isNull();
        assertThat(saved.getRack()).isNull();
        assertThat(saved.getCage()).isNull();
        assertThat(saved.getOrigin()).isNull();
        assertThat(saved.getNote()).isNull();
    }

    @Test
    void shouldUpdateMouse() {

        Strain strain = entityManager.persistFlushFind(
                new Strain("DBA", "DBA/2"));

        Mouse mouse = entityManager.persistFlushFind(
                new Mouse(
                        4001,
                        Mouse.Sex.M,
                        strain,
                        null,
                        null,
                        null,
                        null,
                        LocalDate.of(2026, 1, 1),
                        null,
                        "Room A",
                        "Rack 1",
                        "Cage 1",
                        null,
                        "Initial note"));

        mouse.setRoom("Room B");
        mouse.setRack("Rack 2");
        mouse.setCage("Cage 5");
        mouse.setDeathDate(LocalDate.of(2026, 5, 1));
        mouse.setNote("Updated note");

        entityManager.persistAndFlush(mouse);
        entityManager.clear();

        Mouse saved = entityManager.find(Mouse.class, mouse.getId());

        assertThat(saved.getRoom()).isEqualTo("Room B");
        assertThat(saved.getRack()).isEqualTo("Rack 2");
        assertThat(saved.getCage()).isEqualTo("Cage 5");
        assertThat(saved.getDeathDate()).isEqualTo(LocalDate.of(2026, 5, 1));
        assertThat(saved.getNote()).isEqualTo("Updated note");
    }

    @Test
    void shouldRejectDuplicateAnimalNumber() {

        Strain strain = entityManager.persistFlushFind(
                new Strain("NOD", "Non-obese diabetic"));

        entityManager.persistAndFlush(
                new Mouse(
                        5001,
                        Mouse.Sex.F,
                        strain,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null));

        Mouse duplicate = new Mouse(
                5001,
                Mouse.Sex.M,
                strain,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        assertThatThrownBy(() -> entityManager.persistAndFlush(duplicate))
                .hasRootCauseInstanceOf(org.postgresql.util.PSQLException.class)
                .hasMessageContaining("duplicate key value violates unique constraint");
    }

    @Test
    void shouldRejectNullAnimalNumber() {

        Strain strain = entityManager.persistFlushFind(
                new Strain("AKR", "AKR/J"));

        Mouse mouse = new Mouse(
                null,
                Mouse.Sex.M,
                strain,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        assertThatThrownBy(() -> entityManager.persistAndFlush(mouse))
                .hasRootCauseInstanceOf(org.postgresql.util.PSQLException.class)
                .hasMessageContaining("null value in column \"animal_number\"");
    }

    @Test
    void shouldRejectNullSex() {

        Strain strain = entityManager.persistFlushFind(
                new Strain("129S", "129S strain"));

        Mouse mouse = new Mouse(
                6001,
                null,
                strain,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        assertThatThrownBy(() -> entityManager.persistAndFlush(mouse))
                .hasRootCauseInstanceOf(org.postgresql.util.PSQLException.class)
                .hasMessageContaining("null value in column \"sex\"");
    }

    @Test
    void shouldRejectNullStrainAssociation() {

        Mouse mouse = new Mouse(
                7001,
                Mouse.Sex.F,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        assertThatThrownBy(() -> entityManager.persistAndFlush(mouse))
                .isInstanceOf(org.hibernate.exception.ConstraintViolationException.class)
                .hasMessageContaining("ERROR: null value in column \"strain_id\"");
    }

    @Test
    void shouldRejectDeathDateBeforeBirthDate() {

        Strain strain = entityManager.persistFlushFind(
                new Strain("SJL", "SJL/J"));

        Mouse mouse = new Mouse(
                8001,
                Mouse.Sex.M,
                strain,
                null,
                null,
                null,
                null,
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 4, 1),
                null,
                null,
                null,
                null,
                null);

        assertThatThrownBy(() -> entityManager.persistAndFlush(mouse))
                .hasRootCauseInstanceOf(org.postgresql.util.PSQLException.class)
                .hasMessageContaining("violates check constraint");
    }
}