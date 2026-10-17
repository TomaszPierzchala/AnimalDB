package cz.animalhouse.service

import cz.animalhouse.entity.Mouse
import cz.animalhouse.entity.Strain
import cz.animalhouse.exception.MouseDeleteInUseException
import cz.animalhouse.repository.MouseRepository
import cz.animalhouse.repository.StrainRepository
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
class MouseServiceIntegrationTest {

    @Autowired
    lateinit var mouseService: MouseService

    @Autowired
    lateinit var mouseRepository: MouseRepository

    @Autowired
    lateinit var strainRepository: StrainRepository

    @Autowired
    lateinit var entityManager: EntityManager

    @Test
    fun `delete should throw MouseDeleteInUseException when mouse is used as parent`() {

        val strain = strainRepository.save(
            Strain(
                "TEST",
                "Test strain"
            )
        )

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
}