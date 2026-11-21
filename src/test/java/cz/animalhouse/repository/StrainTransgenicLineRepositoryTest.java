package cz.animalhouse.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.postgresql.PostgreSQLContainer;

import cz.animalhouse.entity.Strain;
import cz.animalhouse.entity.StrainTransgenicLine;
import jakarta.persistence.EntityManager;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class StrainTransgenicLineRepositoryTest {

    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17");

    @Autowired
    private StrainRepository strainRepository;

    @Autowired
    private StrainTransgenicLineRepository transgenicLineRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldInsertTwoTransgenicLinesAndFindAll() {

        Strain strain = new Strain(
                "C57BL6",
                "C57BL/6");

        strainRepository.save(strain);

        StrainTransgenicLine line1 = new StrainTransgenicLine(
                strain,
                "OT-I");

        StrainTransgenicLine line2 = new StrainTransgenicLine(
                strain,
                "GFP");

        transgenicLineRepository.saveAll(
                List.of(line1, line2));

        transgenicLineRepository.flush();
        entityManager.clear();

        List<StrainTransgenicLine> lines =
                transgenicLineRepository.findAll();

        assertThat(lines).hasSize(2);

        assertThat(lines)
                .extracting(StrainTransgenicLine::getName)
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

        StrainTransgenicLine line =
                transgenicLineRepository.save(
                        new StrainTransgenicLine(
                                firstStrain,
                                "OT-I"));

        line.setName("OT-II");
        line.setStrain(secondStrain);

        transgenicLineRepository.flush();
        entityManager.clear();

        StrainTransgenicLine saved =
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

        StrainTransgenicLine line1 =
                transgenicLineRepository.save(
                        new StrainTransgenicLine(
                                strain,
                                "OT-I"));

        transgenicLineRepository.save(
                new StrainTransgenicLine(
                        strain,
                        "OT-II"));

        transgenicLineRepository.flush();
        entityManager.clear();

        transgenicLineRepository.deleteById(line1.getId());

        transgenicLineRepository.flush();
        entityManager.clear();

        List<StrainTransgenicLine> lines =
                transgenicLineRepository.findAll();

        assertThat(lines).hasSize(1);

        assertThat(lines.getFirst().getName())
                .isEqualTo("OT-II");
    }

    @Test
    void shouldCheckWhetherNameExists() {

        Strain strain = strainRepository.saveAndFlush(
                new Strain(
                        "C57BL6",
                        "C57BL/6"));

        transgenicLineRepository.saveAndFlush(
                new StrainTransgenicLine(
                        strain,
                        "OT-I"));

        entityManager.clear();

        assertThat(
                transgenicLineRepository.existsByStrainIdAndName(strain.getId(), "OT-I")
        ).isTrue();

        assertThat(
                transgenicLineRepository.existsByStrainIdAndName(strain.getId(), "Unknown")
        ).isFalse();
    }

    @Test
    void shouldCheckWhetherNameBelongsToAnotherRecord() {

        Strain strain = strainRepository.save(
                new Strain(
                        "C57BL6",
                        "C57BL/6"));

        StrainTransgenicLine line1 =
                transgenicLineRepository.save(
                        new StrainTransgenicLine(
                                strain,
                                "OT-I"));

        StrainTransgenicLine line2 =
                transgenicLineRepository.save(
                        new StrainTransgenicLine(
                                strain,
                                "OT-II"));

        transgenicLineRepository.flush();
        entityManager.clear();

        assertThat(
                transgenicLineRepository
                        .existsByStrainIdAndNameAndIdNot(
                                strain.getId(),
                                "OT-I",
                                line2.getId())
        ).isTrue();

        assertThat(
                transgenicLineRepository
                        .existsByStrainIdAndNameAndIdNot(
                                strain.getId(),
                                "OT-I",
                                line1.getId())
        ).isFalse();
    }
}
