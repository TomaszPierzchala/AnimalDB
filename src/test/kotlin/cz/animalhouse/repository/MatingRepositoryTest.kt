package cz.animalhouse.repository

import cz.animalhouse.entity.Mating
import cz.animalhouse.entity.Mouse
import cz.animalhouse.entity.Strain
import cz.animalhouse.entity.Mouse.Sex
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.assertThrows
import org.springframework.dao.DataIntegrityViolationException
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.testcontainers.postgresql.PostgreSQLContainer
import java.time.LocalDate

import jakarta.persistence.EntityManager
import org.springframework.test.context.ActiveProfiles

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MatingRepositoryTest {

    private val FIXED_TODAY = LocalDate.of(2026, 10, 19)

    companion object {
        @JvmField
        @ServiceConnection
        val postgres = PostgreSQLContainer("postgres:17")
    }

    @Autowired
    lateinit var matingRepository: MatingRepository

    @Autowired
    lateinit var mouseRepository: MouseRepository

    @Autowired
    lateinit var strainRepository: StrainRepository

    @Autowired
    lateinit var entityManager: EntityManager



    @Test
    fun `should save and find mating by id`() {

        val strain = strainRepository.save(
            Strain("C57BL6", "C57BL-6")
        )

        val mother = Mouse(
            1001,
            Sex.F,
            strain,
            birthDate = LocalDate.of(2026, 8, 1)
        )

        val father = Mouse(
            2001,
            Sex.M,
            strain,
            birthDate = LocalDate.of(2026, 7, 21)
        )

        mouseRepository.saveAllAndFlush(listOf(mother, father))

        val mating = Mating(
            female = mother,
            male = father,
            note = "First, test mating"
        )

        val saved = matingRepository.saveAndFlush(mating)
        val savedId = requireNotNull(saved.id)

        entityManager.clear()

        val loaded = matingRepository.findById(savedId)
            .orElseThrow()

        assertThat(loaded.id)
            .isEqualTo(savedId)

        assertThat(loaded.female.id)
            .isEqualTo(mother.id)

        assertThat(loaded.male.id)
            .isEqualTo(father.id)

        assertThat(loaded.female.animalNumber)
            .isEqualTo(1001)

        assertThat(loaded.male.animalNumber)
            .isEqualTo(2001)

        assertThat(loaded.matingDate).isNull()
        assertThat(loaded.plugDate).isNull()
        assertThat(loaded.pregnancyDate).isNull()
        assertThat(loaded.birthDate).isNull()
        assertThat(loaded.weaningDate).isNull()

        assertThat(loaded.bornCount).isNull()
        assertThat(loaded.aliveCount).isNull()
        assertThat(loaded.femaleCount).isNull()
        assertThat(loaded.maleCount).isNull()

        assertThat(loaded.note)
            .isEqualTo("First, test mating")
    }

}