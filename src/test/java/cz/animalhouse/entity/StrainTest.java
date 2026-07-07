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
class StrainTest {

	@ServiceConnection
	static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

	@Autowired
	private TestEntityManager entityManager;

	@Test
	void shouldPersistAndLoadStrain() {

		Strain strain = new Strain("C57BL/6J", "Black 6");

		entityManager.persistAndFlush(strain);
		entityManager.clear();

		Strain saved = entityManager.find(Strain.class, strain.getId());

		assertThat(saved).isNotNull();
		assertThat(saved.getId()).isNotNull();

		assertThat(saved.getCode()).isEqualTo("C57BL/6J");
		assertThat(saved.getName()).isEqualTo("Black 6");

		assertThat(saved).isEqualTo(strain);
		assertThat(saved.hashCode()).isEqualTo(strain.hashCode());

		assertThat(saved.toString()).contains("Strain [id=").contains("C57BL/6J").contains("Black 6");

	}

	@Test
	void shouldUpdateStrain() {

		Strain strain = new Strain("BALB", "BALB");

		entityManager.persistAndFlush(strain);

		strain.setName("BALB/c");

		entityManager.persistAndFlush(strain);
		entityManager.clear();

		Strain saved = entityManager.find(Strain.class, strain.getId());

		assertThat(saved.getName()).isEqualTo("BALB/c");
	}

	@Test
	void shouldRejectDuplicateCode() {

		entityManager.persistAndFlush(new Strain("C57BL/6J", "Black 6"));

		Strain duplicate = new Strain("C57BL/6J", "Another strain");

		assertThatThrownBy(() -> entityManager.persistAndFlush(duplicate))
				.hasRootCauseInstanceOf(org.postgresql.util.PSQLException.class)
				.hasMessageContaining("ERROR: duplicate key value violates unique constraint");

	}

	@Test
	void shouldRejectNullCode() {

		Strain strain = new Strain(null, "Black 6");

		assertThatThrownBy(() -> entityManager.persistAndFlush(strain))
				.hasRootCauseInstanceOf(org.postgresql.util.PSQLException.class)
				.hasMessageContaining("ERROR: null value in column \"code\"");
	}
}