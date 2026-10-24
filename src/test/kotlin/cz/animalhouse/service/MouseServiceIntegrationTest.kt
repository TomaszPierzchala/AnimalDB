package cz.animalhouse.service

import cz.animalhouse.dto.ParentCandidateResponse
import cz.animalhouse.entity.Mouse
import cz.animalhouse.entity.Strain
import cz.animalhouse.exception.MouseDeleteInUseException
import cz.animalhouse.repository.MouseRepository
import cz.animalhouse.repository.StrainRepository
import cz.animalhouse.testutil.TestClock
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import java.time.Clock

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Testcontainers
@Import(MouseServiceIntegrationTest.TestClockConfig::class)
class MouseServiceIntegrationTest {

    companion object {
        val FIXED_TODAY = TestClock.FIXED_TODAY

        @Container
        @ServiceConnection
        @JvmField
        val postgres = PostgreSQLContainer("postgres:17")
    }

    @TestConfiguration
    class TestClockConfig {

        @Bean
        @Primary
        fun fixedTestClock(): Clock = TestClock.clock
    }

    @Autowired
    lateinit var mouseService: MouseService

    @Autowired
    lateinit var mouseRepository: MouseRepository

    @Autowired
    lateinit var strainRepository: StrainRepository

    @Autowired
    lateinit var entityManager: EntityManager

    lateinit var strain: Strain

    @BeforeEach
    fun setUp() {
        strain = strainRepository.save(
            Strain(
                "TEST",
                "Test strain"
            )
        )
    }

    @Test
    fun `delete should reject deleting a mouse referenced as mother`() {

        val mother = mouseRepository.saveAndFlush(
            Mouse(
                animalNumber = 1001,
                sex = Mouse.Sex.F,
                strain = strain
            )
        )

        mouseRepository.saveAndFlush(
            Mouse(
                animalNumber = 1002,
                sex = Mouse.Sex.M,
                strain = strain,
                mother = mother
            )
        )

        entityManager.clear()

        val exception =
            assertThrows<MouseDeleteInUseException> {
                mouseService.delete(
                    requireNotNull(mother.id)
                )
            }

        assertThat(exception.message)
            .isEqualTo(
                "Mouse with Animal number: 1001 cannot be deleted because it is referenced by other records."
            )
    }

    @Test
    fun `service findParentCandidates should filter females by age and death date`() {
        val femaleTooYoung = Mouse(
            animalNumber = 81713011,
            sex = Mouse.Sex.F,
            strain = strain,
            birthDate = FIXED_TODAY.minusWeeks(3)
        )
        val maleTooYoung = Mouse(
            animalNumber = 91713012,
            sex = Mouse.Sex.M,
            strain = strain,
            birthDate = FIXED_TODAY.minusWeeks(2)
        )

        val femaleOldEnough = Mouse(
            animalNumber = 1713003,
            sex = Mouse.Sex.F,
            strain = strain,
            birthDate = FIXED_TODAY.minusWeeks(5)
        )

        val maleOldEnough = Mouse(
            animalNumber = 17130013,
            sex = Mouse.Sex.M,
            strain = strain,
            birthDate = FIXED_TODAY.minusWeeks(5)
        )

        val femaleOldEnoughButYounger = Mouse(
            animalNumber = 61713002,
            sex = Mouse.Sex.F,
            strain = strain,
            birthDate = FIXED_TODAY.minusWeeks(5).plusDays(4)
        )
        val femaleOldEnoughButOlder = Mouse(
            animalNumber = 91713004,
            sex = Mouse.Sex.F,
            strain = strain,
            birthDate = FIXED_TODAY.minusWeeks(5).minusDays(3)
        )

        val femaleNoBirthDate = Mouse(
            animalNumber = 51713001,
            sex = Mouse.Sex.F,
            strain = strain,
            birthDate = null
        )
        val maleNoBirthDate = Mouse(
            animalNumber = 517130014,
            sex = Mouse.Sex.M,
            strain = strain,
            birthDate = null
        )

        val deadFemale = Mouse(
            animalNumber = 217130015,
            sex = Mouse.Sex.F,
            strain = strain,
            birthDate = FIXED_TODAY.minusWeeks(10),
            deathDate = FIXED_TODAY.minusDays(1)
        )

        mouseRepository.saveAllAndFlush(
            listOf(
                maleTooYoung,
                maleOldEnough,
                maleNoBirthDate,
                femaleOldEnoughButOlder,
                femaleTooYoung,
                femaleOldEnough,
                femaleOldEnoughButYounger,
                femaleNoBirthDate,
                deadFemale
            )
        )

        entityManager.clear()

        val parentCandidates: List<ParentCandidateResponse> = mouseService.findParentCandidates("1713", "F")
        assertThat(parentCandidates)
            .extracting<Int> { it.animalNumber }
            .containsExactly(
                51713001,
                61713002,
                1713003,
                91713004
            )
    }

    @Test
    fun `update should modify the note`() {

        val savedMouse = mouseRepository.saveAndFlush(
            Mouse(
                animalNumber = 1002,
                sex = Mouse.Sex.M,
                strain = strain,
                mother = null
            )
        )

        val mother = mouseRepository.saveAndFlush(
            Mouse(
                animalNumber = 1001,
                sex = Mouse.Sex.F,
                strain = strain
            )
        )
        savedMouse.mother = mother
        entityManager.flush()
        entityManager.refresh(savedMouse)

        savedMouse.animalNumber = 1313
        entityManager.flush()
        entityManager.refresh(savedMouse)

        savedMouse.sex = Mouse.Sex.F
        entityManager.flush()
        entityManager.refresh(savedMouse)

        val newStrain = strainRepository.save(
            Strain(
                "NEW",
                "New strain"
            )
        )
        savedMouse.strain = newStrain
        entityManager.flush()
        entityManager.refresh(savedMouse)

        val father = mouseRepository.saveAndFlush(
            Mouse(
                animalNumber = 8901,
                sex = Mouse.Sex.M,
                strain = newStrain
            )
        )
        savedMouse.father = father
        entityManager.flush()
        entityManager.refresh(savedMouse)

        savedMouse.cage = "New Cage"
        entityManager.flush()
        entityManager.refresh(savedMouse)

        savedMouse.room = "Some room"
        entityManager.flush()
        entityManager.refresh(savedMouse)

        savedMouse.birthDate = FIXED_TODAY.minusDays(6)
        entityManager.flush()
        entityManager.refresh(savedMouse)
        entityManager.clear()

        val loaded = mouseRepository.findByAnimalNumber(savedMouse.animalNumber)

        assertThat(loaded!!.animalNumber).isEqualTo(1313)

        assertThat(loaded.note)
            .contains("column Mother Animal Number value changed from NULL to 1001")
            .contains("column Animal Number value changed from 1002 to 1313")
            .contains("column Sex value changed from M to F")
            .contains("column Strain value changed from 'TEST — Test strain' to 'NEW — New strain'")
            .contains("column Father Animal Number value changed from NULL to 8901")
            .contains("column Cage value changed from NULL to New Cage")
            .contains("column Room value changed from NULL to Some room")
            .contains("column Birth Date value changed from NULL to 2026-10-13")

    }
}