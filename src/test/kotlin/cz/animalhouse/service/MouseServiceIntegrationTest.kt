package cz.animalhouse.service

import cz.animalhouse.dto.ParentCandidateResponse
import cz.animalhouse.entity.Mouse
import cz.animalhouse.entity.Strain
import cz.animalhouse.exception.MouseDeleteInUseException
import cz.animalhouse.repository.MouseRepository
import cz.animalhouse.repository.StrainRepository
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@SpringBootTest
@Transactional
class MouseServiceIntegrationTest {
    private val FIXED_TODAY = LocalDate.of(2026, 10, 19)

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
    fun `delete should throw MouseDeleteInUseException when mouse is used as parent`() {

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
            animalNumber = 27230015,
            sex = Mouse.Sex.F,
            strain = strain,
            birthDate = FIXED_TODAY.minusWeeks(10),
            deathDate = FIXED_TODAY.minusDays(1)
        )

        mouseRepository.saveAll(
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
}