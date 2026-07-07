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
class PersonTest {

    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17");

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldPersistAndLoadPerson() {

        Person person = new Person(
                "John Smith",
                "Laboratory technician");

        entityManager.persistAndFlush(person);
        entityManager.clear();

        Person saved = entityManager.find(Person.class, person.getId());

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();

        assertThat(saved.getName()).isEqualTo("John Smith");
        assertThat(saved.getDescription())
                .isEqualTo("Laboratory technician");

        assertThat(saved).isEqualTo(person);
        assertThat(saved.hashCode()).isEqualTo(person.hashCode());

        assertThat(saved.toString())
                .contains("Person [")
                .contains("John Smith")
                .contains("Laboratory technician");
    }

    @Test
    void shouldUpdatePerson() {

        Person person = new Person(
                "John Smith",
                "Laboratory technician");

        entityManager.persistAndFlush(person);

        person.setName("John Doe");
        person.setDescription("Animal facility director");

        entityManager.persistAndFlush(person);
        entityManager.clear();

        Person saved = entityManager.find(Person.class, person.getId());

        assertThat(saved.getName()).isEqualTo("John Doe");
        assertThat(saved.getDescription())
                .isEqualTo("Animal facility director");
    }

    @Test
    void shouldAllowNullDescription() {

        Person person = new Person(
                "John Smith",
                null);

        entityManager.persistAndFlush(person);
        entityManager.clear();

        Person saved = entityManager.find(Person.class, person.getId());

        assertThat(saved.getDescription()).isNull();
    }

    @Test
    void shouldRejectNullName() {

        Person person = new Person(
                null,
                "Laboratory technician");

        assertThatThrownBy(() ->
                entityManager.persistAndFlush(person))
                .hasRootCauseInstanceOf(org.postgresql.util.PSQLException.class)
                .hasMessageContaining("null value in column \"name\"");
    }
}