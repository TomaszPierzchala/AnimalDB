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

        assertThat(saved.male).isNotNull()
        assertThat(saved.male.animalNumber)
            .isEqualTo(1001)

        assertThat(saved.male.sex)
            .isEqualTo(Mouse.Sex.M)

        assertThat(saved.female).isNotNull()
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
    fun `should allow optional dates counts and note to be null`() {
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

        assertThat(saved.male).isNotNull()
        assertThat(saved.female).isNotNull()

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

    @Test
    fun `should update mating`() {
        val strain = entityManager.persistFlushFind(
            Strain("DBA", "DBA/2")
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

        val unkle = Mouse(
            animalNumber = 3017,
            sex = Mouse.Sex.M,
            strain = strain
        )
        entityManager.persistAndFlush(unkle)

        mating.male = unkle

        mating.matingDate = LocalDate.of(2026, 5, 1)
        mating.plugDate = LocalDate.of(2026, 5, 3)
        mating.pregnancyDate = LocalDate.of(2026, 5, 13)
        mating.birthDate = LocalDate.of(2026, 6, 4)
        mating.weaningDate = LocalDate.of(2026, 6, 25)

        mating.bornCount = 13
        mating.aliveCount = 12
        mating.femaleCount = 7
        mating.maleCount = 5
        mating.note = "Some note"

        entityManager.flush()
        entityManager.clear()

        val saved = requireNotNull(
            entityManager.find(
                Mating::class.java,
                requireNotNull(mating.id)
            )
        )

        assertThat(saved.female).isEqualTo(mother)

        assertThat(saved.male).isEqualTo(unkle)

        assertThat(saved.matingDate).isEqualTo(LocalDate.of(2026, 5, 1))

        assertThat(saved.plugDate).isEqualTo(LocalDate.of(2026, 5, 3))
        assertThat(saved.pregnancyDate).isEqualTo(LocalDate.of(2026, 5, 13))
        assertThat(saved.birthDate).isEqualTo(LocalDate.of(2026, 6, 4))
        assertThat(saved.weaningDate).isEqualTo(LocalDate.of(2026, 6, 25))
        assertThat(saved.endMatingDate).isEqualTo(LocalDate.of(2026, 6, 25))

        assertThat(saved.bornCount).isEqualTo(13)
        assertThat(saved.aliveCount).isEqualTo(12)
        assertThat(saved.femaleCount).isEqualTo(7)
        assertThat(saved.maleCount).isEqualTo(5)

        assertThat(saved.note).isEqualTo("Some note")

    }

    @Test
    fun `should reject null as a male`() {
        val strain = entityManager.persistFlushFind(
            Strain(
                "NOD",
                "Non-obese diabetic"
            )
        )
        val female = Mouse(
            animalNumber = 5001,
            sex = Mouse.Sex.F,
            strain = strain
        )
        entityManager.persistAndFlush(female)

        val mating = Mating(
            female = female,
            male = female,
        )
        ReflectionTestUtils.setField(
            mating,
            "male",
            null
        )

        assertThatThrownBy {
            entityManager.persistAndFlush(mating)
        }
            .hasRootCauseInstanceOf(
                PSQLException::class.java
            )
            .hasMessageContaining(
                "ERROR: null value in column \"male_id\" of relation \"mating\" violates not-null constraint"
            )
    }

    @Test
    fun `should reject null as a female`() {
        val strain = entityManager.persistFlushFind(
            Strain(
                "NOD",
                "Non-obese diabetic"
            )
        )
        val male = Mouse(
            animalNumber = 301,
            sex = Mouse.Sex.M,
            strain = strain
        )
        entityManager.persistAndFlush(male)

        val mating = Mating(
            female = male,
            male = male,
        )
        ReflectionTestUtils.setField(
            mating,
            "female",
            null
        )

        assertThatThrownBy {
            entityManager.persistAndFlush(mating)
        }
            .hasRootCauseInstanceOf(
                PSQLException::class.java
            )
            .hasMessageContaining(
                "ERROR: null value in column \"female_id\" of relation \"mating\" violates not-null constraint"
            )
    }

    @Test
    fun `should reject same mouse as male and female`() {
        val strain = entityManager.persistFlushFind(
            Strain(
                "NOD",
                "Non-obese diabetic"
            )
        )
        val male = Mouse(
            animalNumber = 301,
            sex = Mouse.Sex.M,
            strain = strain
        )
        entityManager.persistAndFlush(male)

        val mating = Mating(
            female = male,
            male = male,
        )

        assertThatThrownBy {
            entityManager.persistAndFlush(mating)
        }
            .hasRootCauseInstanceOf(
                PSQLException::class.java
            )
            .hasMessageContaining(
                "ERROR: new row for relation \"mating\" violates check constraint"
            )
    }

    @Test
    fun `should reject mating_date not before plug_date`() {
        val strain = entityManager.persistFlushFind(
            Strain(
                "NOD",
                "Non-obese diabetic"
            )
        )
        val male = Mouse(
            animalNumber = 301,
            sex = Mouse.Sex.M,
            strain = strain
        )
        entityManager.persistAndFlush(male)
        val female = Mouse(
            animalNumber = 1301,
            sex = Mouse.Sex.F,
            strain = strain
        )
        entityManager.persistAndFlush(female)

        val mating = Mating(
            female = female,
            male = male,
            matingDate = LocalDate.of(2026, 5, 1),
            plugDate = LocalDate.of(2026, 4, 30)
        )

        assertThatThrownBy {
            entityManager.persistAndFlush(mating)
        }
            .hasRootCauseInstanceOf(
                PSQLException::class.java
            )
            .hasMessageContaining(
                "ERROR: new row for relation \"mating\" violates check constraint"
            )
    }

    @Test
    fun `should reject plug_date not before pregnancy_date`() {
        val strain = entityManager.persistFlushFind(
            Strain(
                "NOD",
                "Non-obese diabetic"
            )
        )
        val male = Mouse(
            animalNumber = 301,
            sex = Mouse.Sex.M,
            strain = strain
        )
        entityManager.persistAndFlush(male)
        val female = Mouse(
            animalNumber = 1301,
            sex = Mouse.Sex.F,
            strain = strain
        )
        entityManager.persistAndFlush(female)

        val mating = Mating(
            female = female,
            male = male,

            plugDate = LocalDate.of(2026, 4, 30),
            pregnancyDate = LocalDate.of(2026, 4, 27)
            )

        assertThatThrownBy {
            entityManager.persistAndFlush(mating)
        }
            .hasRootCauseInstanceOf(
                PSQLException::class.java
            )
            .hasMessageContaining(
                "ERROR: new row for relation \"mating\" violates check constraint"
            )
    }

    @Test
    fun `should reject pregnancy_date not before birth_date`() {
        val strain = entityManager.persistFlushFind(
            Strain(
                "NOD",
                "Non-obese diabetic"
            )
        )
        val male = Mouse(
            animalNumber = 301,
            sex = Mouse.Sex.M,
            strain = strain
        )
        entityManager.persistAndFlush(male)
        val female = Mouse(
            animalNumber = 1301,
            sex = Mouse.Sex.F,
            strain = strain
        )
        entityManager.persistAndFlush(female)

        val mating = Mating(
            female = female,
            male = male,

            pregnancyDate = LocalDate.of(2026, 4, 27),
            birthDate = LocalDate.of(2026, 4, 23)
            )

        assertThatThrownBy {
            entityManager.persistAndFlush(mating)
        }
            .hasRootCauseInstanceOf(
                PSQLException::class.java
            )
            .hasMessageContaining(
                "ERROR: new row for relation \"mating\" violates check constraint"
            )
    }

    @Test
    fun `should reject birth_date not before weaning_date`() {
        val strain = entityManager.persistFlushFind(
            Strain(
                "NOD",
                "Non-obese diabetic"
            )
        )
        val male = Mouse(
            animalNumber = 301,
            sex = Mouse.Sex.M,
            strain = strain
        )
        entityManager.persistAndFlush(male)
        val female = Mouse(
            animalNumber = 1301,
            sex = Mouse.Sex.F,
            strain = strain
        )
        entityManager.persistAndFlush(female)

        val mating = Mating(
            female = female,
            male = male,

            birthDate = LocalDate.of(2026, 4, 23),
            weaningDate = LocalDate.of(2026, 4, 2)
            )

        assertThatThrownBy {
            entityManager.persistAndFlush(mating)
        }
            .hasRootCauseInstanceOf(
                PSQLException::class.java
            )
            .hasMessageContaining(
                "ERROR: new row for relation \"mating\" violates check constraint"
            )
    }

    @Test
    fun `should reject born_count negative`() {
        val strain = entityManager.persistFlushFind(
            Strain(
                "NOD",
                "Non-obese diabetic"
            )
        )
        val male = Mouse(
            animalNumber = 301,
            sex = Mouse.Sex.M,
            strain = strain
        )
        entityManager.persistAndFlush(male)
        val female = Mouse(
            animalNumber = 1301,
            sex = Mouse.Sex.F,
            strain = strain
        )
        entityManager.persistAndFlush(female)

        val mating = Mating(
            female = female,
            male = male,

            bornCount = -1,
            aliveCount = null,
            femaleCount = null,
            maleCount = null,
        )

        assertThatThrownBy {
            entityManager.persistAndFlush(mating)
        }
            .hasRootCauseInstanceOf(
                PSQLException::class.java
            )
            .hasMessageContaining(
                "ERROR: new row for relation \"mating\" violates check constraint \"mating_born_count_check\""
            )
    }

    @Test
    fun `should reject alive_count negative`() {
        val strain = entityManager.persistFlushFind(
            Strain(
                "NOD",
                "Non-obese diabetic"
            )
        )
        val male = Mouse(
            animalNumber = 301,
            sex = Mouse.Sex.M,
            strain = strain
        )
        entityManager.persistAndFlush(male)
        val female = Mouse(
            animalNumber = 1301,
            sex = Mouse.Sex.F,
            strain = strain
        )
        entityManager.persistAndFlush(female)

        val mating = Mating(
            female = female,
            male = male,

            bornCount = null,
            aliveCount = -1,
            femaleCount = null,
            maleCount = null,
        )

        assertThatThrownBy {
            entityManager.persistAndFlush(mating)
        }
            .hasRootCauseInstanceOf(
                PSQLException::class.java
            )
            .hasMessageContaining(
                "ERROR: new row for relation \"mating\" violates check constraint \"mating_alive_count_check\""
            )
    }

    @Test
    fun `should reject female_count negative`() {
        val strain = entityManager.persistFlushFind(
            Strain(
                "NOD",
                "Non-obese diabetic"
            )
        )
        val male = Mouse(
            animalNumber = 301,
            sex = Mouse.Sex.M,
            strain = strain
        )
        entityManager.persistAndFlush(male)
        val female = Mouse(
            animalNumber = 1301,
            sex = Mouse.Sex.F,
            strain = strain
        )
        entityManager.persistAndFlush(female)

        val mating = Mating(
            female = female,
            male = male,

            bornCount = null,
            aliveCount = null,
            femaleCount = -1,
            maleCount = null,
        )

        assertThatThrownBy {
            entityManager.persistAndFlush(mating)
        }
            .hasRootCauseInstanceOf(
                PSQLException::class.java
            )
            .hasMessageContaining(
                "ERROR: new row for relation \"mating\" violates check constraint \"mating_female_count_check\""
            )
    }

    @Test
    fun `should reject male_count negative`() {
        val strain = entityManager.persistFlushFind(
            Strain(
                "NOD",
                "Non-obese diabetic"
            )
        )
        val male = Mouse(
            animalNumber = 301,
            sex = Mouse.Sex.M,
            strain = strain
        )
        entityManager.persistAndFlush(male)
        val female = Mouse(
            animalNumber = 1301,
            sex = Mouse.Sex.F,
            strain = strain
        )
        entityManager.persistAndFlush(female)

        val mating = Mating(
            female = female,
            male = male,

            bornCount = null,
            aliveCount = null,
            femaleCount = null,
            maleCount = -1,
        )

        assertThatThrownBy {
            entityManager.persistAndFlush(mating)
        }
            .hasRootCauseInstanceOf(
                PSQLException::class.java
            )
            .hasMessageContaining(
                "ERROR: new row for relation \"mating\" violates check constraint \"mating_male_count_check\""
            )
    }

    @Test
    fun `should reject alive_count bigger then born_count`() {
        val strain = entityManager.persistFlushFind(
            Strain(
                "NOD",
                "Non-obese diabetic"
            )
        )
        val male = Mouse(
            animalNumber = 301,
            sex = Mouse.Sex.M,
            strain = strain
        )
        entityManager.persistAndFlush(male)
        val female = Mouse(
            animalNumber = 1301,
            sex = Mouse.Sex.F,
            strain = strain
        )
        entityManager.persistAndFlush(female)

        val mating = Mating(
            female = female,
            male = male,

            bornCount = 10,
            aliveCount = 11,
            femaleCount = null,
            maleCount = null,
        )

        assertThatThrownBy {
            entityManager.persistAndFlush(mating)
        }
            .hasRootCauseInstanceOf(
                PSQLException::class.java
            )
            .hasMessageContaining(
                "ERROR: new row for relation \"mating\" violates check constraint"
            )
    }

    @Test
    fun `should reject female_count bigger then alive_count`() {
        val strain = entityManager.persistFlushFind(
            Strain(
                "NOD",
                "Non-obese diabetic"
            )
        )
        val male = Mouse(
            animalNumber = 301,
            sex = Mouse.Sex.M,
            strain = strain
        )
        entityManager.persistAndFlush(male)
        val female = Mouse(
            animalNumber = 1301,
            sex = Mouse.Sex.F,
            strain = strain
        )
        entityManager.persistAndFlush(female)

        val mating = Mating(
            female = female,
            male = male,

            bornCount = null,
            aliveCount = 11,
            femaleCount = 12,
            maleCount = null,
        )

        assertThatThrownBy {
            entityManager.persistAndFlush(mating)
        }
            .hasRootCauseInstanceOf(
                PSQLException::class.java
            )
            .hasMessageContaining(
                "ERROR: new row for relation \"mating\" violates check constraint"
            )
    }

    @Test
    fun `should reject male_count bigger then alive_count`() {
        val strain = entityManager.persistFlushFind(
            Strain(
                "NOD",
                "Non-obese diabetic"
            )
        )
        val male = Mouse(
            animalNumber = 301,
            sex = Mouse.Sex.M,
            strain = strain
        )
        entityManager.persistAndFlush(male)
        val female = Mouse(
            animalNumber = 1301,
            sex = Mouse.Sex.F,
            strain = strain
        )
        entityManager.persistAndFlush(female)

        val mating = Mating(
            female = female,
            male = male,

            bornCount = null,
            aliveCount = 11,
            femaleCount = null,
            maleCount = 12,
        )

        assertThatThrownBy {
            entityManager.persistAndFlush(mating)
        }
            .hasRootCauseInstanceOf(
                PSQLException::class.java
            )
            .hasMessageContaining(
                "ERROR: new row for relation \"mating\" violates check constraint"
            )
    }

    @Test
    fun `should reject sum of female_count and male_count bigger then alive_count`() {
        val strain = entityManager.persistFlushFind(
            Strain(
                "NOD",
                "Non-obese diabetic"
            )
        )
        val male = Mouse(
            animalNumber = 301,
            sex = Mouse.Sex.M,
            strain = strain
        )
        entityManager.persistAndFlush(male)
        val female = Mouse(
            animalNumber = 1301,
            sex = Mouse.Sex.F,
            strain = strain
        )
        entityManager.persistAndFlush(female)

        val mating = Mating(
            female = female,
            male = male,

            bornCount = null,
            aliveCount = 11,
            femaleCount = 5,
            maleCount = 7,
        )

        assertThatThrownBy {
            entityManager.persistAndFlush(mating)
        }
            .hasRootCauseInstanceOf(
                PSQLException::class.java
            )
            .hasMessageContaining(
                "ERROR: new row for relation \"mating\" violates check constraint"
            )
    }

    @Test
    fun `should reject nonexistent female_id`() {
        val strain = entityManager.persistFlushFind(
            Strain("NOD", "Non-obese diabetic")
        )

        val male = entityManager.persistFlushFind(
            Mouse(
                animalNumber = 301,
                sex = Mouse.Sex.M,
                strain = strain
            )
        )

        val nonexistentFemale = entityManager.entityManager.getReference(
            Mouse::class.java,
            999999L
        )

        val mating = Mating(
            female = nonexistentFemale,
            male = male
        )

        assertThatThrownBy {
            entityManager.persistAndFlush(mating)
        }
            .hasRootCauseInstanceOf(PSQLException::class.java)
            .hasMessageContaining("violates foreign key constraint")
    }

    @Test
    fun `should reject nonexistent male_id`() {
        val strain = entityManager.persistFlushFind(
            Strain("NOD", "Non-obese diabetic")
        )

        val female = entityManager.persistFlushFind(
            Mouse(
                animalNumber = 1301,
                sex = Mouse.Sex.F,
                strain = strain
            )
        )

        val nonexistentMale = entityManager.entityManager.getReference(
            Mouse::class.java,
            999999L
        )

        val mating = Mating(
            female = female,
            male = nonexistentMale
        )

        assertThatThrownBy {
            entityManager.persistAndFlush(mating)
        }
            .hasRootCauseInstanceOf(PSQLException::class.java)
            .hasMessageContaining("violates foreign key constraint")
    }

    @Test
    fun `should allow missing intermediate dates`() {
        val strain = entityManager.persistFlushFind(
            Strain("NOD", "Non-obese diabetic")
        )

        val female = entityManager.persistFlushFind(
            Mouse(
                animalNumber = 1301,
                sex = Mouse.Sex.F,
                strain = strain
            )
        )

        val male = entityManager.persistFlushFind(
            Mouse(
                animalNumber = 301,
                sex = Mouse.Sex.M,
                strain = strain
            )
        )

        val mating = Mating(
            female = female,
            male = male,
            matingDate = LocalDate.of(2026, 5, 1),
            plugDate = null,
            pregnancyDate = null,
            birthDate = LocalDate.of(2026, 6, 1),
            weaningDate = LocalDate.of(2026, 6, 22)
        )

        entityManager.persistAndFlush(mating)
        entityManager.clear()

        val saved = requireNotNull(
            entityManager.find(
                Mating::class.java,
                requireNotNull(mating.id)
            )
        )

        assertThat(saved.matingDate)
            .isEqualTo(LocalDate.of(2026, 5, 1))

        assertThat(saved.plugDate).isNull()
        assertThat(saved.pregnancyDate).isNull()

        assertThat(saved.birthDate)
            .isEqualTo(LocalDate.of(2026, 6, 1))

        assertThat(saved.weaningDate)
            .isEqualTo(LocalDate.of(2026, 6, 22))

        assertThat(saved.endMatingDate)
            .isEqualTo(LocalDate.of(2026, 6, 22))
    }

    @Test
    fun `should delete mating without deleting mice`() {
        val strain = entityManager.persistFlushFind(
            Strain("NOD", "Non-obese diabetic")
        )

        val female = entityManager.persistFlushFind(
            Mouse(
                animalNumber = 1301,
                sex = Mouse.Sex.F,
                strain = strain
            )
        )

        val male = entityManager.persistFlushFind(
            Mouse(
                animalNumber = 301,
                sex = Mouse.Sex.M,
                strain = strain
            )
        )

        val mating = entityManager.persistFlushFind(
            Mating(
                female = female,
                male = male
            )
        )

        val matingId = requireNotNull(mating.id)
        val femaleId = requireNotNull(female.id)
        val maleId = requireNotNull(male.id)

        entityManager.remove(mating)
        entityManager.flush()
        entityManager.clear()

        assertThat(
            entityManager.find(Mating::class.java, matingId)
        ).isNull()

        assertThat(
            entityManager.find(Mouse::class.java, femaleId)
        ).isNotNull()

        assertThat(
            entityManager.find(Mouse::class.java, maleId)
        ).isNotNull()
    }
 }