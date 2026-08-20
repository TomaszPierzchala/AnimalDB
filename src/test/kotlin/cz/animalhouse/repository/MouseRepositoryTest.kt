package cz.animalhouse.repository

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

import jakarta.persistence.EntityManager;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MouseRepositoryTest {

    companion object {
        @JvmField
        @ServiceConnection
        val postgres = PostgreSQLContainer("postgres:17")
    }

    @Autowired
    lateinit var mouseRepository: MouseRepository

    @Autowired
    lateinit var strainRepository: StrainRepository
    
    @Autowired
    lateinit var entityManager: EntityManager


    @Test
    fun `should save and find mouse by id`() {
        val strain = strainRepository.save(
            Strain(
                "C57BL6",
                "C57BL-6"
            )
        )

        val mouse = Mouse(
            1001,
            Sex.M,
            strain,
            null,
            null,
            null,
            null,
            LocalDate.of(2026, 8, 1),
            null,
            "Room 1",
            "Rack A",
            "Cage 12",
            "Prague",
            "Test mouse"
        )

        val saved = mouseRepository.save(mouse)

        mouseRepository.flush()
        entityManager.clear()

        val found = mouseRepository.findById(saved.id)

        assertThat(found).isPresent

        val loaded = found.orElseThrow()

        assertThat(loaded.id)
            .isEqualTo(saved.id)

        assertThat(loaded.animalNumber)
            .isEqualTo(1001)

        assertThat(loaded.sex)
            .isEqualTo(Sex.M)

        assertThat(loaded.strain.id)
            .isEqualTo(strain.id)

        assertThat(loaded.birthDate)
            .isEqualTo(LocalDate.of(2026, 8, 1))

        assertThat(loaded.room)
            .isEqualTo("Room 1")

        assertThat(loaded.rack)
            .isEqualTo("Rack A")

        assertThat(loaded.cage)
            .isEqualTo("Cage 12")

        assertThat(loaded.origin)
            .isEqualTo("Prague")

        assertThat(loaded.note)
            .isEqualTo("Test mouse")
    }

    @Test
    fun `should find all mice`() {
        val strain = strainRepository.save(
            Strain(
                "BALBC",
                "BALB-c"
            )
        )

        mouseRepository.save(
            Mouse(
                2001,
                Sex.F,
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
                null
            )
        )

        mouseRepository.save(
            Mouse(
                2002,
                Sex.M,
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
                null
            )
        )

        mouseRepository.flush()
        entityManager.clear()

        val result = mouseRepository.findAll()

        assertThat(result)
            .hasSize(2)

        assertThat(result)
            .extracting<Int> { it.animalNumber }
            .containsExactlyInAnyOrder(
                2001,
                2002
            )
    }

    @Test
    fun `should not allow duplicate animal number`() {
        val strain = strainRepository.save(
            Strain(
                "C57",
                "C57"
            )
        )

        val first = Mouse(
            3001,
            Sex.M,
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
            null
        )

        val second = Mouse(
            3001,
            Sex.F,
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
            null
        )

        mouseRepository.save(first)
        mouseRepository.flush()

        assertThrows<DataIntegrityViolationException> {
            mouseRepository.saveAndFlush(second)
        }
    }
}