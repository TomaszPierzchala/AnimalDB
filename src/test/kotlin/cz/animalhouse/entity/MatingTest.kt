package cz.animalhouse.entity

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.postgresql.PostgreSQLContainer
import java.time.LocalDate
import javax.sql.DataSource

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(
    replace = AutoConfigureTestDatabase.Replace.NONE
)
class MatingTest {

    companion object {
        @ServiceConnection
        @JvmField
        val postgres =
            PostgreSQLContainer("postgres:17")
    }

    @Autowired
    lateinit var entityManager: TestEntityManager

    @Autowired
    lateinit var dataSource: DataSource

    @Test
    fun `should use test PostgreSQL`() {
        dataSource.connection.use { connection ->
            println("JDBC URL: ${connection.metaData.url}")
            println("Database: ${connection.metaData.databaseProductName}")
            println("Container running: ${postgres.isRunning}")
            println("Container ID: ${postgres.containerId}")
            println("Container JDBC URL: ${postgres.jdbcUrl}")

            assertThat(connection.metaData.databaseProductName)
                .isEqualTo("PostgreSQL")

            assertThat(connection.metaData.url)
                .isEqualTo(postgres.jdbcUrl)
        }
    }

    @Test
    fun `should persist and load mating`() {
        val strain = entityManager.persistFlushFind(
            Strain("C57BL/6J", "Black 6")
        )

        val father = Mouse(
            animalNumber = 1001,
            sex = Mouse.Sex.M,
            strain = strain,
            birthDate = LocalDate.of(2026, 1, 10),
            room = "Room A",
            rack = "Rack 1",
            cage = "Cage 12",
            origin = "Charles University",
            note = "Healthy mouse"
        )
        entityManager.persistAndFlush(father)

        val mother = Mouse(
            animalNumber = 3013,
            sex = Mouse.Sex.F,
            strain = strain,
            birthDate = LocalDate.of(2026, 2, 20),
            room = "Room A",
            rack = "Rack 2",
            cage = "Cage 17",
            origin = "Charles University",
            note = "Healthy mouse"
        )
        entityManager.persistAndFlush(mother)

        val mating = Mating(
            female = mother,
            male = father,
            matingDate = LocalDate.of(2026, 5, 17),
            plugDate = LocalDate.of(2026, 5, 18),
            pregnancyDate = LocalDate.of(2026, 5, 22),
            birthDate = LocalDate.of(2026, 6, 6),
            weaningDate = LocalDate.of(2026, 6, 29),
            bornCount = 10,
            aliveCount = 8,
            femaleCount = 4,
            maleCount = 2,
            note = "Everything is fine"
        )

        entityManager.persistAndFlush(mating)
        entityManager.clear()

        val saved = requireNotNull(
            entityManager.find(
                Mating::class.java,
                requireNotNull(mating.id)
            )
        )

        assertThat(saved).isNotNull()
        assertThat(saved.id).isNotNull()

        assertThat { saved.male }.isNotNull()
        assertThat(saved.male.animalNumber)
            .isEqualTo(1001)

        assertThat(saved.male.sex)
            .isEqualTo(Mouse.Sex.M)

        assertThat { saved.female }.isNotNull()
        assertThat(saved.female.animalNumber)
            .isEqualTo(3013)

        assertThat(saved.female.sex)
            .isEqualTo(Mouse.Sex.F)


        assertThat(saved.matingDate).isEqualTo(LocalDate.of(2026, 5, 17))
        assertThat(saved.plugDate).isEqualTo(LocalDate.of(2026, 5, 18),)
        assertThat(saved.pregnancyDate).isEqualTo(LocalDate.of(2026, 5, 22))
        assertThat(saved.birthDate).isEqualTo(LocalDate.of(2026, 6, 6))
        assertThat(saved.weaningDate).isEqualTo(LocalDate.of(2026, 6, 29))
        assertThat(saved.endMatingDate).isEqualTo(LocalDate.of(2026, 6, 29))

        assertThat(saved.bornCount).isEqualTo(10)
        assertThat(saved.aliveCount).isEqualTo(8)
        assertThat(saved.femaleCount).isEqualTo(4)
        assertThat(saved.maleCount).isEqualTo(2)
        assertThat(saved.note).isEqualTo("Everything is fine")

        // compares by entity.id
        assertThat(saved).isEqualTo(mating)

        assertThat(saved.hashCode()).isEqualTo(mating.hashCode())

        assertThat(saved.toString())
            .contains("Mating [(id=")
            .contains("), (female=Mouse [(")
            .contains("), animalNumber=3013, sex=F]), (male=Mouse [(")
            .contains("), animalNumber=1001, sex=M]), note=Everything is fine)]")
    }

    @Test
    fun `should allow optional relations and text fields to be null`() {
        val strain = entityManager.persistFlushFind(
            Strain("FVB", "Friend Virus B")
        )

        val mother = Mouse(
            animalNumber = 3001,
            sex = Mouse.Sex.F,
            strain = strain
        )
        entityManager.persistAndFlush(mother)

        val father = Mouse(
            animalNumber = 3009,
            sex = Mouse.Sex.M,
            strain = strain
        )
        entityManager.persistAndFlush(father)
        entityManager.clear()

        val mating = Mating(
            female = mother,
            male = father,
            matingDate = null,
            plugDate = null,
            pregnancyDate = null,
            birthDate = null,
            weaningDate = null,
            bornCount = null,
            aliveCount = null,
            femaleCount = null,
            maleCount = null,
            note = null
        )

        entityManager.persistAndFlush(mating)
        entityManager.clear()

        val saved = requireNotNull(
            entityManager.find(
                Mating::class.java,
                requireNotNull(mating.id)
            )
        )

        assertThat(saved).isNotNull()
        assertThat(saved.id).isNotNull()

        assertThat { saved.male }.isNotNull()
        assertThat { saved.female }.isNotNull()

        assertThat(saved.matingDate).isNull()
        assertThat(saved.plugDate).isNull()
        assertThat(saved.pregnancyDate).isNull()
        assertThat(saved.birthDate).isNull()
        assertThat(saved.weaningDate).isNull()
        assertThat(saved.endMatingDate).isNull()

        assertThat(saved.bornCount).isNull()
        assertThat(saved.aliveCount).isNull()
        assertThat(saved.femaleCount).isNull()
        assertThat(saved.maleCount).isNull()
        assertThat(saved.note).isNull()
    }

}