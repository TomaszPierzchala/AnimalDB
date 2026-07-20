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
import cz.animalhouse.entity.TransgenicLine;
import jakarta.persistence.EntityManager;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class TransgenicLineRepositoryTest {

    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17");

    @Autowired
    private StrainRepository strainRepository;

    @Autowired
    private TransgenicLineRepository transgenicLineRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldInsertTwoTransgenicLinesAndFindAll() {

        Strain strain = new Strain(
                "C57BL6",
                "C57BL/6");

        strainRepository.save(strain);

        TransgenicLine line1 = new TransgenicLine(
                strain,
                "OT-I");

        TransgenicLine line2 = new TransgenicLine(
                strain,
                "GFP");

        transgenicLineRepository.saveAll(
                List.of(line1, line2));

        transgenicLineRepository.flush();
        entityManager.clear();

        List<TransgenicLine> lines =
                transgenicLineRepository.findAll();

        assertThat(lines).hasSize(2);

        assertThat(lines)
                .extracting(TransgenicLine::getName)
                .containsExactlyInAnyOrder(
                        "OT-I",
                        "GFP");

        assertThat(lines)
                .extracting(line -> line.getStrain().getCode())
                .containsOnly("C57BL6");

        assertThat(line1.getId()).isNotNull();
        assertThat(line2.getId()).isNotNull();
    }

    @Test
    void shouldUpdateTransgenicLine() {

        Strain firstStrain = strainRepository.save(
                new Strain(
                        "C57BL6",
                        "C57BL/6"));

        Strain secondStrain = strainRepository.save(
                new Strain(
                        "BALBC",
                        "BALB/c"));

        TransgenicLine line =
                transgenicLineRepository.save(
                        new TransgenicLine(
                                firstStrain,
                                "OT-I"));

        line.setName("OT-II");
        line.setStrain(secondStrain);

        transgenicLineRepository.flush();
        entityManager.clear();

        TransgenicLine saved =
                transgenicLineRepository.findById(line.getId())
                        .orElseThrow();

        assertThat(saved.getName())
                .isEqualTo("OT-II");

        assertThat(saved.getStrain().getId())
                .isEqualTo(secondStrain.getId());

        assertThat(saved.getStrain().getCode())
                .isEqualTo("BALBC");
    }

    @Test
    void shouldDeleteTransgenicLine() {

        Strain strain = strainRepository.save(
                new Strain(
                        "C57BL6",
                        "C57BL/6"));

        TransgenicLine line1 =
                transgenicLineRepository.save(
                        new TransgenicLine(
                                strain,
                                "OT-I"));

        transgenicLineRepository.save(
                new TransgenicLine(
                        strain,
                        "OT-II"));

        transgenicLineRepository.flush();
        entityManager.clear();

        transgenicLineRepository.deleteById(line1.getId());

        transgenicLineRepository.flush();
        entityManager.clear();

        List<TransgenicLine> lines =
                transgenicLineRepository.findAll();

        assertThat(lines).hasSize(1);

        assertThat(lines.get(0).getName())
                .isEqualTo("OT-II");
    }

    @Test
    void shouldCheckWhetherNameExists() {

        Strain strain = strainRepository.save(
                new Strain(
                        "C57BL6",
                        "C57BL/6"));

        transgenicLineRepository.save(
                new TransgenicLine(
                        strain,
                        "OT-I"));

        assertThat(
                transgenicLineRepository.existsByName("OT-I")
        ).isTrue();

        assertThat(
                transgenicLineRepository.existsByName("Unknown")
        ).isFalse();
    }

    @Test
    void shouldCheckWhetherNameBelongsToAnotherRecord() {

        Strain strain = strainRepository.save(
                new Strain(
                        "C57BL6",
                        "C57BL/6"));

        TransgenicLine line1 =
                transgenicLineRepository.save(
                        new TransgenicLine(
                                strain,
                                "OT-I"));

        TransgenicLine line2 =
                transgenicLineRepository.save(
                        new TransgenicLine(
                                strain,
                                "OT-II"));

        assertThat(
                transgenicLineRepository
                        .existsByNameAndIdNot(
                                "OT-I",
                                line2.getId())
        ).isTrue();

        assertThat(
                transgenicLineRepository
                        .existsByNameAndIdNot(
                                "OT-I",
                                line1.getId())
        ).isFalse();
    }
}
