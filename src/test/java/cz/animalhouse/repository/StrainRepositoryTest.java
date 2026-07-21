package cz.animalhouse.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.postgresql.PostgreSQLContainer;

import cz.animalhouse.entity.Strain;
import jakarta.persistence.EntityManager;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class StrainRepositoryTest {

    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17");

    @Autowired
    private StrainRepository strainRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldInsertTwoStrainsAndFindAll() {

        Strain strain1 = new Strain(
                "C57BL6",
                "C57BL/6");

        Strain strain2 = new Strain(
                "BALBC",
                "BALB/c");

        strainRepository.saveAll(List.of(strain1, strain2));

        strainRepository.flush();
        entityManager.clear();

        List<Strain> strains = strainRepository.findAll();

        assertThat(strains).hasSize(2);

        assertThat(strains)
                .extracting(Strain::getCode)
                .containsExactlyInAnyOrder(
                        "C57BL6",
                        "BALBC");

        assertThat(strains)
                .extracting(Strain::getName)
                .containsExactlyInAnyOrder(
                        "C57BL/6",
                        "BALB/c");

        assertThat(strain1.getId()).isNotNull();
        assertThat(strain2.getId()).isNotNull();
    }

    @Test
    void shouldUpdateStrainAndFindIt() {

        Strain strain = new Strain(
                "C57BL6",
                "C57BL/6");

        strainRepository.save(strain);

        strain.setCode("C57BL6J");
        strain.setName("C57BL/6J");

        strainRepository.flush();
        entityManager.clear();

        Strain saved = strainRepository.findById(strain.getId())
                .orElseThrow();

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCode()).isEqualTo("C57BL6J");
        assertThat(saved.getName()).isEqualTo("C57BL/6J");
    }

    @Test
    void shouldDeleteOneStrainAndFindRemaining() {

        Strain strain1 = new Strain(
                "C57BL6",
                "C57BL/6");

        Strain strain2 = new Strain(
                "BALBC",
                "BALB/c");

        strainRepository.saveAll(List.of(strain1, strain2));

        strainRepository.flush();
        entityManager.clear();

        strainRepository.deleteById(strain1.getId());

        strainRepository.flush();
        entityManager.clear();

        List<Strain> strains = strainRepository.findAll();

        assertThat(strains).hasSize(1);

        Strain remainingStrain = strains.get(0);

        assertThat(remainingStrain.getCode())
                .isEqualTo("BALBC");

        assertThat(remainingStrain.getName())
                .isEqualTo("BALB/c");
    }

    @Test
    void shouldReturnTrueWhenCodeExists() {

        Strain strain = new Strain(
                "C57BL6",
                "C57BL/6");

        strainRepository.save(strain);

        boolean exists = strainRepository.existsByCode("C57BL6");

        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseWhenCodeDoesNotExist() {

        Strain strain = new Strain(
                "C57BL6",
                "C57BL/6");

        strainRepository.save(strain);

        boolean exists = strainRepository.existsByCode("BALBC");

        assertThat(exists).isFalse();
    }

    @Test
    void shouldReturnTrueWhenCodeBelongsToAnotherStrain() {

        Strain strain1 = strainRepository.save(
                new Strain(
                        "C57BL6",
                        "C57BL/6"));

        Strain strain2 = strainRepository.save(
                new Strain(
                        "BALBC",
                        "BALB/c"));

        boolean exists = strainRepository.existsByCodeAndIdNot(
                "C57BL6",
                strain2.getId());

        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseWhenCodeBelongsToTheSameStrain() {

        Strain strain = strainRepository.save(
                new Strain(
                        "C57BL6",
                        "C57BL/6"));

        boolean exists = strainRepository.existsByCodeAndIdNot(
                "C57BL6",
                strain.getId());

        assertThat(exists).isFalse();
    }
}