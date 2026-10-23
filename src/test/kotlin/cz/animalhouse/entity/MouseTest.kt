package cz.animalhouse.entity

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.postgresql.util.PSQLException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.util.ReflectionTestUtils
import org.testcontainers.postgresql.PostgreSQLContainer
import java.time.LocalDate
import javax.sql.DataSource

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(
    replace = AutoConfigureTestDatabase.Replace.NONE
)
class MouseTest {

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
    fun `should persist and load mouse`() {
        val strain = entityManager.persistFlushFind(
            Strain("C57BL/6J", "Black 6")
        )

        val director = entityManager.persistFlushFind(
            Person(
                "Director",
                "Procedure director"
            )
        )

        val procedure = entityManager.persistFlushFind(
            LabProcedure(
                "P-32-2026",
                "Test procedure",
                "Test description",
                director,
                LocalDate.of(2026, 1, 1),
                null
            )
        )

        val transgenicLine =
            entityManager.persistFlushFind(
                TransgenicLine(
                    strain,
                    "OT-I"
                )
            )

        val mouse = Mouse(
            animalNumber = 1001,
            sex = Mouse.Sex.M,
            strain = strain,
            transgenicLine = transgenicLine,
            labProcedure = procedure,
            birthDate = LocalDate.of(2026, 1, 10),
            room = "Room A",
            rack = "Rack 1",
            cage = "Cage 12",
            origin = "Charles University",
            note = "Healthy mouse"
        )

        entityManager.persistAndFlush(mouse)
        entityManager.clear()

        val saved = requireNotNull(
            entityManager.find(
                Mouse::class.java,
                requireNotNull(mouse.id)
            )
        )

        assertThat(saved).isNotNull()
        assertThat(saved.id).isNotNull()

        assertThat(saved.animalNumber)
            .isEqualTo(1001)

        assertThat(saved.sex)
            .isEqualTo(Mouse.Sex.M)

        assertThat(saved.strain.id)
            .isEqualTo(strain.id)

        assertThat(saved.transgenicLine?.id)
            .isEqualTo(transgenicLine.id)

        assertThat(saved.labProcedure?.id)
            .isEqualTo(procedure.id)

        assertThat(saved.birthDate)
            .isEqualTo(LocalDate.of(2026, 1, 10))

        assertThat(saved.deathDate)
            .isNull()

        assertThat(saved.room)
            .isEqualTo("Room A")

        assertThat(saved.rack)
            .isEqualTo("Rack 1")

        assertThat(saved.cage)
            .isEqualTo("Cage 12")

        assertThat(saved.origin)
            .isEqualTo("Charles University")

        assertThat(saved.note)
            .isEqualTo("Healthy mouse")

        assertThat(saved)
            .isEqualTo(mouse)

        assertThat(saved.hashCode())
            .isEqualTo(mouse.hashCode())

        assertThat(saved.toString())
            .contains("Mouse [(id=")
            .contains("animalNumber=1001")
            .contains("sex=M")
    }

    @Test
    fun `should persist mouse with parents`() {
        val strain = entityManager.persistFlushFind(
            Strain("BALB", "BALB/c")
        )

        val mother = entityManager.persistFlushFind(
            Mouse(
                animalNumber = 2001,
                sex = Mouse.Sex.F,
                strain = strain,
                birthDate = LocalDate.of(2025, 1, 1),
                room = "Room A",
                rack = "Rack 1",
                cage = "Cage 1",
                note = "Mother mouse"
            )
        )

        val father = entityManager.persistFlushFind(
            Mouse(
                animalNumber = 2002,
                sex = Mouse.Sex.M,
                strain = strain,
                birthDate = LocalDate.of(2025, 1, 2),
                room = "Room A",
                rack = "Rack 1",
                cage = "Cage 1",
                note = "Father mouse"
            )
        )

        val child = Mouse(
            animalNumber = 2003,
            sex = Mouse.Sex.F,
            strain = strain,
            mother = mother,
            father = father,
            birthDate = LocalDate.of(2026, 1, 10),
            room = "Room B",
            rack = "Rack 2",
            cage = "Cage 3",
            note = "Child mouse"
        )

        entityManager.persistAndFlush(child)
        entityManager.clear()

        val saved = requireNotNull(
            entityManager.find(
                Mouse::class.java,
                requireNotNull(child.id)
            )
        )

        assertThat(saved).isNotNull()

        assertThat(saved.mother?.id)
            .isEqualTo(mother.id)

        assertThat(saved.father?.id)
            .isEqualTo(father.id)

        assertThat(saved.mother?.animalNumber)
            .isEqualTo(2001)

        assertThat(saved.father?.animalNumber)
            .isEqualTo(2002)
    }

    @Test
    fun `should allow optional relations and text fields to be null`() {
        val strain = entityManager.persistFlushFind(
            Strain("FVB", "Friend Virus B")
        )

        val mouse = Mouse(
            animalNumber = 3001,
            sex = Mouse.Sex.F,
            strain = strain
        )

        entityManager.persistAndFlush(mouse)
        entityManager.clear()

        val saved = requireNotNull(
            entityManager.find(
                Mouse::class.java,
                requireNotNull(mouse.id)
            )
        )

        assertThat(saved).isNotNull()

        assertThat(saved.transgenicLine).isNull()
        assertThat(saved.labProcedure).isNull()
        assertThat(saved.mother).isNull()
        assertThat(saved.father).isNull()

        assertThat(saved.birthDate).isNull()
        assertThat(saved.deathDate).isNull()

        assertThat(saved.room).isNull()
        assertThat(saved.rack).isNull()
        assertThat(saved.cage).isNull()
        assertThat(saved.origin).isNull()
        assertThat(saved.note).isNull()
    }

    @Test
    fun `should update mouse`() {
        val strain = entityManager.persistFlushFind(
            Strain("DBA", "DBA/2")
        )
        val newStrain = entityManager.persistFlushFind(
            Strain("NEW", "New/13")
        )

        val mouse = entityManager.persistFlushFind(
            Mouse(
                animalNumber = 4001,
                sex = Mouse.Sex.M,
                strain = strain,
                birthDate = LocalDate.of(2026, 1, 1),
                room = "Room A",
                rack = "Rack 1",
                cage = "Cage 1",
                note = "Initial note"
            )
        )

        mouse.room = "Room B"
        mouse.rack = "Rack 2"
        mouse.cage = "Cage 5"
        mouse.deathDate = LocalDate.of(2026, 5, 1)
        mouse.note = "Updated note"
        mouse.strain = newStrain

        entityManager.persistAndFlush(mouse)
        entityManager.clear()

        val saved = requireNotNull(
            entityManager.find(
                Mouse::class.java,
                requireNotNull(mouse.id)
            )
        )

        assertThat(saved.room)
            .isEqualTo("Room B")

        assertThat(saved.rack)
            .isEqualTo("Rack 2")

        assertThat(saved.cage)
            .isEqualTo("Cage 5")

        assertThat(saved.deathDate)
            .isEqualTo(LocalDate.of(2026, 5, 1))

        assertThat(saved.note)
            .contains("column Cage value changed from Cage 1 to Cage 5")
            .contains("column Death Date value changed from NULL to 2026-05-01")
            .contains("column Rack value changed from Rack 1 to Rack 2")
            .contains("column Room value changed from Room A to Room B")
            .contains("column Strain value changed from 'DBA — DBA/2' to 'NEW — New/13'")
            .contains("Updated note")
            .doesNotContain("Initial note")
    }

    @Test
    fun `should reject duplicate animal number`() {
        val strain = entityManager.persistFlushFind(
            Strain(
                "NOD",
                "Non-obese diabetic"
            )
        )

        entityManager.persistAndFlush(
            Mouse(
                animalNumber = 5001,
                sex = Mouse.Sex.F,
                strain = strain
            )
        )

        val duplicate = Mouse(
            animalNumber = 5001,
            sex = Mouse.Sex.M,
            strain = strain
        )

        assertThatThrownBy {
            entityManager.persistAndFlush(duplicate)
        }
            .hasRootCauseInstanceOf(
                PSQLException::class.java
            )
            .hasMessageContaining(
                "duplicate key value violates unique constraint"
            )
    }

    @Test
    fun `should reject null sex at database level`() {
        val strain = entityManager.persistFlushFind(
            Strain("129S", "129S strain")
        )

        val mouse = Mouse(
            animalNumber = 6001,
            sex = Mouse.Sex.M,
            strain = strain
        )

        ReflectionTestUtils.setField(
            mouse,
            "sex",
            null
        )

        assertThatThrownBy {
            entityManager.persistAndFlush(mouse)
        }
            .hasRootCauseInstanceOf(
                PSQLException::class.java
            )
            .hasMessageContaining(
                "null value in column \"sex\""
            )
    }

    @Test
    fun `should reject null strain association at database level`() {
        val strain = entityManager.persistFlushFind(
            Strain("TEMP", "Temporary strain")
        )

        val mouse = Mouse(
            animalNumber = 7001,
            sex = Mouse.Sex.F,
            strain = strain
        )

        ReflectionTestUtils.setField(
            mouse,
            "strain",
            null
        )

        assertThatThrownBy {
            entityManager.persistAndFlush(mouse)
        }
        .isInstanceOf(org.hibernate.exception.ConstraintViolationException::class.java)
        .hasMessageContaining("ERROR: null value in column \"strain_id\"")
    }

    @Test
    fun `should reject death date before birth date`() {
        val strain = entityManager.persistFlushFind(
            Strain("SJL", "SJL/J")
        )

        val mouse = Mouse(
            animalNumber = 8001,
            sex = Mouse.Sex.M,
            strain = strain,
            birthDate = LocalDate.of(2026, 5, 1),
            deathDate = LocalDate.of(2026, 4, 1)
        )

        assertThatThrownBy {
            entityManager.persistAndFlush(mouse)
        }
            .hasRootCauseInstanceOf(
                PSQLException::class.java
            )
            .hasMessageContaining(
                "violates check constraint"
            )
    }
}