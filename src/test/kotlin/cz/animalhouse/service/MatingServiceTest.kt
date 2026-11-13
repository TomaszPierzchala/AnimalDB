
package cz.animalhouse.service

import cz.animalhouse.dto.MatingRequest
import cz.animalhouse.entity.Mating
import cz.animalhouse.entity.Mouse
import cz.animalhouse.repository.MatingRepository
import cz.animalhouse.repository.MouseRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.kotlin.any
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.time.LocalDate

class MatingServiceTest {

    private val matingRepository = mock<MatingRepository>()
    private val mouseRepository = mock<MouseRepository>()
    private val mouseService = mock<MouseService>()

    private val matingService = MatingService(
        matingRepository,
        mouseRepository,
        mouseService
    )

    @Test
    fun `create rejects dates in wrong order when intermediate dates are null`() {
        val request = validRequest().copy(
            matingDate = LocalDate.of(2026, 10, 10),
            plugDate = null,
            pregnancyDate = LocalDate.of(2026, 10, 5),
            birthDate = null,
            weaningDate = LocalDate.of(2026, 11, 1)
        )

        val exception = assertThrows<IllegalArgumentException> {
            matingService.create(request)
        }

        assertEquals(
            "matingDate must be before pregnancyDate",
            exception.message
        )

        verifyNoInteractions(
            matingRepository,
            mouseRepository,
            mouseService
        )
    }

    @Test
    fun `create throws IllegalArgumentException when femaleAnimalNumber is null`() {
        val request = validRequest().copy(
            femaleAnimalNumber = null
        )

        val exception = assertThrows<IllegalArgumentException> {
            matingService.create(request)
        }

        assertEquals(
            "Mouse animal number must not be null",
            exception.message
        )

        verify(mouseService, never()).findMouseByAnimalNumber(isNull())
        verifyNoInteractions(matingRepository)
    }

    @ParameterizedTest
    @CsvSource(
        "M, M, Female mouse must have sex F",
        "F, F, Male mouse must have sex M",
        "M, F, Female mouse must have sex F"
    )
    fun `create rejects invalid mouse sexes`(
        femaleSex: String,
        maleSex: String,
        expectedMessage: String
    ) {
        val female = mock<Mouse>()
        val male = mock<Mouse>()

        whenever(female.sex).thenReturn(Mouse.Sex.valueOf(femaleSex))
        whenever(male.sex).thenReturn(Mouse.Sex.valueOf(maleSex))

        whenever(mouseService.findMouseByAnimalNumber(1001))
            .thenReturn(female)
        whenever(mouseService.findMouseByAnimalNumber(1002))
            .thenReturn(male)

        val exception = assertThrows<IllegalArgumentException> {
            matingService.create(validRequest())
        }

        assertEquals(expectedMessage, exception.message)
        verifyNoInteractions(matingRepository)
    }

    @Test
    fun `create accepts correct mouse sexes`() {
        val female = mock<Mouse>()
        val male = mock<Mouse>()

        whenever(female.sex).thenReturn(Mouse.Sex.F)
        whenever(male.sex).thenReturn(Mouse.Sex.M)
        whenever(female.animalNumber).thenReturn(1001)
        whenever(male.animalNumber).thenReturn(1002)

        whenever(mouseService.findMouseByAnimalNumber(1001))
            .thenReturn(female)
        whenever(mouseService.findMouseByAnimalNumber(1002))
            .thenReturn(male)

        val mating = mock<Mating>()
        whenever(mating.id).thenReturn(1L)
        whenever(mating.female).thenReturn(female)
        whenever(mating.male).thenReturn(male)

        whenever(matingRepository.save(any<Mating>()))
            .thenReturn(mating)

        val response = matingService.create(validRequest())

        assertEquals(1L, response.id)
        assertEquals(1001, response.femaleAnimalNumber)
        assertEquals(1002, response.maleAnimalNumber)

        verify(matingRepository).save(any<Mating>())
    }

    private fun validRequest() = MatingRequest(
        femaleAnimalNumber = 1001,
        maleAnimalNumber = 1002,
        matingDate = null,
        plugDate = null,
        pregnancyDate = null,
        birthDate = null,
        weaningDate = null,
        bornCount = null,
        aliveCount = null,
        femaleCount = null,
        maleCount = null,
        note = null,
        endMatingDate = null
    )
}
