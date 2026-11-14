
package cz.animalhouse.service

import cz.animalhouse.dto.MatingRequest
import cz.animalhouse.entity.Mating
import cz.animalhouse.entity.Mouse
import cz.animalhouse.repository.MatingRepository
import cz.animalhouse.repository.MouseRepository
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.kotlin.*
import org.springframework.test.util.ReflectionTestUtils
import java.time.LocalDate
import java.util.Optional

class MatingServiceTest {

    private val matingRepository = mock<MatingRepository>()
    private val mouseRepository = mock<MouseRepository>()
    private val mouseService = mock<MouseService>()
    private val entityManager = mock<EntityManager>()

    private val matingService = MatingService(
        matingRepository,
        mouseRepository,
        mouseService,
        entityManager
    )

    private val female = mock<Mouse>()
    private val male = mock<Mouse>()

    private fun mockMice() {
        whenever(female.sex).thenReturn(Mouse.Sex.F)
        whenever(male.sex).thenReturn(Mouse.Sex.M)

        whenever(female.animalNumber).thenReturn(1001)
        whenever(male.animalNumber).thenReturn(1002)

        whenever(mouseService.findMouseByAnimalNumber(1001))
            .thenReturn(female)
        whenever(mouseService.findMouseByAnimalNumber(1002))
            .thenReturn(male)
    }

    private fun mockSave(id: Long = 1L) {
        whenever(matingRepository.saveAndFlush(any<Mating>()))
            .thenAnswer { invocation ->
                invocation.getArgument<Mating>(0).apply {
                    ReflectionTestUtils.setField(this, "id", id)
                }
            }
    }

    @Test
    fun `create saves mating and refreshes entity`() {
        mockMice()
        mockSave()

        val request = validRequest().copy(
            matingDate = LocalDate.of(2026, 10, 1),
            plugDate = LocalDate.of(2026, 10, 2),
            bornCount = 5,
            aliveCount = 4,
            femaleCount = 2,
            maleCount = 2,
            note = "First mating"
        )

        val response = matingService.create(request)

        assertEquals(1L, response.id)
        assertEquals(1001, response.femaleAnimalNumber)
        assertEquals(1002, response.maleAnimalNumber)
        assertEquals(request.matingDate, response.matingDate)
        assertEquals(request.plugDate, response.plugDate)
        assertEquals(5, response.bornCount)
        assertEquals(4, response.aliveCount)
        assertEquals(2, response.femaleCount)
        assertEquals(2, response.maleCount)
        assertEquals("First mating", response.note)

        val captor = argumentCaptor<Mating>()
        verify(matingRepository).saveAndFlush(captor.capture())

        val saved = captor.firstValue
        assertSame(female, saved.female)
        assertSame(male, saved.male)

        verify(entityManager).refresh(saved)
    }

    @Test
    fun `create rejects nonexistent female mouse`() {
        whenever(mouseService.findMouseByAnimalNumber(1001))
            .thenThrow(
                NoSuchElementException(
                    "Mouse with animal number 1001 does not exist."
                )
            )

        val exception = assertThrows<NoSuchElementException> {
            matingService.create(validRequest())
        }

        assertEquals(
            "Mouse with animal number 1001 does not exist.",
            exception.message
        )

        verify(matingRepository, never()).saveAndFlush(any<Mating>())
        verifyNoInteractions(entityManager)
    }

    @Test
    fun `create rejects nonexistent male mouse`() {
        mockMice()

        whenever(mouseService.findMouseByAnimalNumber(1002))
            .thenThrow(
                NoSuchElementException(
                    "Mouse with animal number 1002 does not exist."
                )
            )

        val exception = assertThrows<NoSuchElementException> {
            matingService.create(validRequest())
        }

        assertEquals(
            "Mouse with animal number 1002 does not exist.",
            exception.message
        )

        verify(matingRepository, never()).saveAndFlush(any<Mating>())
        verifyNoInteractions(entityManager)
    }

    @Test
    fun `update modifies existing mating and refreshes entity`() {
        mockMice()
        mockSave(id = 10L)

        val mating = Mating(female = female, male = male)
        ReflectionTestUtils.setField(mating, "id", 10L)

        whenever(matingRepository.findById(10L))
            .thenReturn(Optional.of(mating))

        val request = validRequest().copy(
            matingDate = LocalDate.of(2026, 10, 1),
            birthDate = LocalDate.of(2026, 10, 22),
            bornCount = 6,
            aliveCount = 5,
            femaleCount = 3,
            maleCount = 2,
            note = "Updated mating"
        )

        val response = matingService.update(10L, request)

        assertEquals(10L, response.id)
        assertEquals(1001, response.femaleAnimalNumber)
        assertEquals(1002, response.maleAnimalNumber)
        assertEquals(request.matingDate, response.matingDate)
        assertEquals(request.birthDate, response.birthDate)
        assertEquals(6, response.bornCount)
        assertEquals(5, response.aliveCount)
        assertEquals(3, response.femaleCount)
        assertEquals(2, response.maleCount)
        assertEquals("Updated mating", response.note)

        verify(matingRepository).findById(10L)
        verify(matingRepository).saveAndFlush(mating)
        verify(entityManager).refresh(mating)
    }

    @Test
    fun `update throws when mating does not exist`() {
        whenever(matingRepository.findById(999L))
            .thenReturn(Optional.empty())

        val exception = assertThrows<NoSuchElementException> {
            matingService.update(999L, validRequest())
        }

        assertEquals(
            "Mating with id=999 not found",
            exception.message
        )

        verify(matingRepository).findById(999L)
        verify(matingRepository, never())
            .saveAndFlush(any<Mating>())
        verifyNoInteractions(mouseService, entityManager)
    }

    @Test
    fun `update rejects invalid dates without changing entity`() {
        val mating = Mating(female = female, male = male)
        ReflectionTestUtils.setField(mating, "id", 10L)

        whenever(matingRepository.findById(10L))
            .thenReturn(Optional.of(mating))

        val request = validRequest().copy(
            matingDate = LocalDate.of(2026, 10, 10),
            pregnancyDate = LocalDate.of(2026, 10, 5)
        )

        val exception = assertThrows<IllegalArgumentException> {
            matingService.update(10L, request)
        }

        assertEquals(
            "matingDate must be before pregnancyDate",
            exception.message
        )

        assertNull(mating.matingDate)
        assertNull(mating.pregnancyDate)

        verify(matingRepository, never())
            .saveAndFlush(any<Mating>())
        verifyNoInteractions(mouseService, entityManager)
    }

    @Test
    fun `update rejects biologically impossible counts`() {
        val mating = Mating(female = female, male = male)
        ReflectionTestUtils.setField(mating, "id", 10L)

        whenever(matingRepository.findById(10L))
            .thenReturn(Optional.of(mating))

        val request = validRequest().copy(
            bornCount = 21
        )

        val exception = assertThrows<IllegalArgumentException> {
            matingService.update(10L, request)
        }

        assertEquals(
            "Biology limits bornCount to values no more then 20",
            exception.message
        )

        verify(matingRepository, never())
            .saveAndFlush(any<Mating>())
        verifyNoInteractions(mouseService, entityManager)
    }

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
        mockMice()
        mockSave()

        val response = matingService.create(validRequest())

        assertEquals(1L, response.id)
        assertEquals(1001, response.femaleAnimalNumber)
        assertEquals(1002, response.maleAnimalNumber)

        verify(matingRepository).saveAndFlush(any<Mating>())
        verify(entityManager).refresh(any<Mating>())
    }


    @ParameterizedTest
    @ValueSource(strings = [
        "bornCount",
        "aliveCount",
        "femaleCount",
        "maleCount"
    ])
    fun `create rejects negative counts`(field: String) {
        val request = validRequest().withCount(field, -1)

        val exception = assertThrows<IllegalArgumentException> {
            matingService.create(request)
        }

        assertEquals(
            "$field cannot be negative",
            exception.message
        )

        verifyNoInteractions(matingRepository, mouseService)
    }

    @ParameterizedTest
    @ValueSource(ints = [21, Int.MAX_VALUE])
    fun `create rejects counts exceeding biological limit`(count: Int) {
        listOf(
            "bornCount",
            "aliveCount",
            "femaleCount",
            "maleCount"
        ).forEach { field ->

            val request = validRequest().withCount(field, count)

            val exception = assertThrows<IllegalArgumentException> {
                matingService.create(request)
            }

            assertEquals(
                "Biology limits $field to values no more then 20",
                exception.message
            )
        }

        verifyNoInteractions(matingRepository, mouseService)
    }


    @ParameterizedTest
    @CsvSource(
        "5, 6, 2, 2, aliveCount must not exceed bornCount",
        "5, 4, 5, 0, femaleCount must not exceed aliveCount",
        "5, 4, 0, 5, maleCount must not exceed aliveCount",
        "5, 4, 3, 2, femaleCount + maleCount must not exceed aliveCount"
    )
    fun `create rejects inconsistent counts`(
        born: Int,
        alive: Int,
        female: Int,
        male: Int,
        expectedMessage: String
    ) {
        val request = validRequest().copy(
            bornCount = born,
            aliveCount = alive,
            femaleCount = female,
            maleCount = male
        )

        val exception = assertThrows<IllegalArgumentException> {
            matingService.create(request)
        }

        assertEquals(expectedMessage, exception.message)
        verifyNoInteractions(matingRepository, mouseService, entityManager)
    }


    @Test
    fun `create rejects same female and male`() {
        val request = validRequest().copy(
            femaleAnimalNumber = 1001,
            maleAnimalNumber = 1001
        )

        val exception = assertThrows<IllegalArgumentException> {
            matingService.create(request)
        }

        assertEquals(
            "Female and male must be different mice",
            exception.message
        )

        verifyNoInteractions(
            matingRepository,
            mouseService,
            entityManager
        )
    }

    @ParameterizedTest
    @CsvSource(
        "matingDate, plugDate",
        "plugDate, pregnancyDate",
        "pregnancyDate, birthDate",
        "birthDate, weaningDate"
    )
    fun `create rejects equal consecutive dates`(
        firstField: String,
        secondField: String
    ) {
        val date = LocalDate.of(2026, 10, 10)

        val request = when (firstField) {
            "matingDate" -> validRequest().copy(
                matingDate = date,
                plugDate = date
            )
            "plugDate" -> validRequest().copy(
                plugDate = date,
                pregnancyDate = date
            )
            "pregnancyDate" -> validRequest().copy(
                pregnancyDate = date,
                birthDate = date
            )
            "birthDate" -> validRequest().copy(
                birthDate = date,
                weaningDate = date
            )
            else -> error("Unknown field: $firstField")
        }

        val exception = assertThrows<IllegalArgumentException> {
            matingService.create(request)
        }

        assertEquals(
            "$firstField must be before $secondField",
            exception.message
        )

        verifyNoInteractions(matingRepository, mouseService)
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

    private fun MatingRequest.withCount(
        field: String,
        value: Int
    ): MatingRequest = when (field) {
        "bornCount" -> copy(bornCount = value)
        "aliveCount" -> copy(aliveCount = value)
        "femaleCount" -> copy(femaleCount = value)
        "maleCount" -> copy(maleCount = value)
        else -> error("Unknown count field: $field")
    }
}
