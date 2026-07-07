package cz.animalhouse.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.postgresql.PostgreSQLContainer;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class LabProcedureTechnicianTest {

	@ServiceConnection
	static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

	@Autowired
	private TestEntityManager entityManager;

	@Test
	void shouldPersistLabProcedureTechnician() {
		Person director = entityManager.persistFlushFind(new Person("Director", "Procedure director"));

		Person technician = entityManager.persistFlushFind(new Person("Technician", "Procedure technician"));

		LabProcedure procedure = entityManager.persistFlushFind(new LabProcedure("P-32-2026", "Test procedure",
				"Test description", director, LocalDate.of(2026, 1, 1), null));

		LabProcedureTechnician assignment = new LabProcedureTechnician(procedure, technician, LocalDate.of(2026, 1, 2),
				"Initial assignment");

		entityManager.persistAndFlush(assignment);
		entityManager.clear();

		LabProcedureTechnicianId id = new LabProcedureTechnicianId(procedure.getId(), technician.getId());

		LabProcedureTechnician saved = entityManager.find(LabProcedureTechnician.class, id);

		assertThat(saved).isNotNull();
		assertThat(saved.getId()).isEqualTo(id);
		assertThat(saved.getAssignedDate()).isEqualTo(LocalDate.of(2026, 1, 2));
		assertThat(saved.getNote()).isEqualTo("Initial assignment");
		assertThat(saved.toString())
		        .contains("LabProcedureTechnician [EmbeddedId=").contains("(labProcedureId=")
		        .contains(", personId=").contains(", assignedDate=2026-01-02");
	}

	@Test
	void shouldRejectNullLabProcedureAssociation() {

		Person technician = entityManager.persistFlushFind(new Person("Technician", "Procedure technician"));

		final LabProcedure NULL_PROCEDURE = null;

		LabProcedureTechnician assignment = new LabProcedureTechnician(NULL_PROCEDURE, technician,
				LocalDate.of(2026, 1, 2), "Initial assignment");

		assertThatThrownBy(() -> entityManager.persistAndFlush(assignment))
				.isInstanceOf(org.hibernate.id.IdentifierGenerationException.class)
				.hasMessageContaining("Could not assign id from null association 'labProcedure'");
	}

	@Test
	void shouldRejectNullTechnicianAssociation() {

		Person director = entityManager.persistFlushFind(new Person("Director", "Procedure director"));

		LabProcedure procedure = entityManager.persistFlushFind(new LabProcedure("P-32-2026", "Test procedure",
				"Test description", director, LocalDate.of(2026, 1, 1), null));

		final Person NULL_TECHNICIAN = null;

		LabProcedureTechnician assignment = new LabProcedureTechnician(procedure, NULL_TECHNICIAN,
				LocalDate.of(2026, 1, 2), "Initial assignment");

		assertThatThrownBy(() -> entityManager.persistAndFlush(assignment))
				.isInstanceOf(org.hibernate.id.IdentifierGenerationException.class)
				.hasMessageContaining("Could not assign id from null association 'technician'");
	}

	@Test
	void shouldAllowNullNote() {
		Person director = entityManager.persistFlushFind(new Person("Director", "Procedure director"));

		Person technician = entityManager.persistFlushFind(new Person("Technician", "Procedure technician"));

		LabProcedure procedure = entityManager.persistFlushFind(new LabProcedure("P-32-2026", "Test procedure",
				"Test description", director, LocalDate.of(2026, 1, 1), null));
		final String nullNote = null;

		LabProcedureTechnician assignment = new LabProcedureTechnician(procedure, technician, LocalDate.of(2026, 7, 6),
				nullNote);

		entityManager.persistAndFlush(assignment);
		entityManager.clear();

		LabProcedureTechnicianId id = new LabProcedureTechnicianId(procedure.getId(), technician.getId());

		LabProcedureTechnician saved = entityManager.find(LabProcedureTechnician.class, id);

		assertThat(saved).isNotNull();
		assertThat(saved.getId()).isEqualTo(id);
		assertThat(saved.getAssignedDate()).isEqualTo(LocalDate.of(2026, 7, 6));
		// note can be null
		assertThat(saved.getNote()).isNull();
	}

	@Test
	void shouldRejectDuplicateLabProcedureTechnician() {
		Person director = entityManager.persistFlushFind(new Person("Director", "Procedure director"));

		Person technician = entityManager.persistFlushFind(new Person("Technician", "Procedure technician"));

		LabProcedure procedure = entityManager.persistFlushFind(new LabProcedure("P-32-2026", "Test procedure",
				"Test description", director, LocalDate.of(2026, 1, 1), null));
		final String nullNote = null;

		LabProcedureTechnician assignment = new LabProcedureTechnician(procedure, technician, LocalDate.of(2026, 5, 3),
				nullNote);

		entityManager.persistAndFlush(assignment);
		entityManager.clear();

		procedure = entityManager.find(LabProcedure.class, procedure.getId());
		technician = entityManager.find(Person.class, technician.getId());

		LabProcedureTechnician duplicate =
		        new LabProcedureTechnician(procedure, technician, LocalDate.of(2026, 7, 6), null);

		assertThatThrownBy(() -> entityManager.persistAndFlush(duplicate))
		        .hasRootCauseInstanceOf(org.postgresql.util.PSQLException.class)
		        .hasMessageContaining("duplicate key value violates unique constraint");

	}
}