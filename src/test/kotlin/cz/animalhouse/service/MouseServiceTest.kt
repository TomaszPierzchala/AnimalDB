package cz.animalhouse.service

import cz.animalhouse.dto.MouseRequest
import cz.animalhouse.dto.MouseResponse
import cz.animalhouse.entity.LabProcedure
import cz.animalhouse.entity.Mouse
import cz.animalhouse.entity.Strain
import cz.animalhouse.entity.TransgenicLine
import cz.animalhouse.exception.DuplicateMouseAnimalNumberException
// import cz.animalhouse.repository.LabProcedureRepository
import cz.animalhouse.repository.MouseRepository
import cz.animalhouse.repository.StrainRepository
import cz.animalhouse.repository.TransgenicLineRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.test.util.ReflectionTestUtils
import java.time.LocalDate
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class MouseServiceTest {

    @Mock
    private lateinit var mouseRepository: MouseRepository

    @Mock
    private lateinit var strainRepository: StrainRepository

    @Mock
    private lateinit var transgenicLineRepository: TransgenicLineRepository

//     @Mock
//     private lateinit var labProcedureRepository: LabProcedureRepository

    @Mock
    private lateinit var strain: Strain

    @Mock
    private lateinit var transgenicLine: TransgenicLine

    @Mock
    private lateinit var labProcedure: LabProcedure

    private lateinit var mouseService: MouseService

    @BeforeEach
    fun setUp() {
        mouseService = MouseService(
            mouseRepository,
            strainRepository,
            transgenicLineRepository//,
            //labProcedureRepository
        )
    }

    private fun mockStrainId() {
     whenever(strain.id).thenReturn(10L)
}

    @Test
    fun shouldFindAllMice() {
        val mouse1 = createMouse(
            id = 1L,
            animalNumber = 1001,
            sex = Mouse.Sex.M
        )

        val mouse2 = createMouse(
            id = 2L,
            animalNumber = 1002,
            sex = Mouse.Sex.F
        )

        whenever(mouseRepository.findAll())
            .thenReturn(listOf(mouse1, mouse2))

        val result: List<MouseResponse> =
            mouseService.findAll()

        assertThat(result).hasSize(2)

        assertThat(result.map { it.animalNumber })
        .containsExactly(
                1001,
                1002
        )

        assertThat(result.map { it.sex })
        .containsExactly(
                Mouse.Sex.M,
                Mouse.Sex.F
        )
    
        verify(mouseRepository).findAll()
    }

    @Test
    fun shouldFindMouseById() {
        val id = 13L

        mockStrainId()
        val mouse = createMouse(
            id = id,
            animalNumber = 1001,
            sex = Mouse.Sex.M
        )

        whenever(mouseRepository.findById(id))
            .thenReturn(Optional.of(mouse))

        val result =
            mouseService.findById(id)

        assertThat(result.id)
            .isEqualTo(id)

        assertThat(result.animalNumber)
            .isEqualTo(1001)

        assertThat(result.sex)
            .isEqualTo(Mouse.Sex.M)

        assertThat(result.strainId)
            .isEqualTo(10L)

        verify(mouseRepository).findById(id)
    }

    @Test
    fun shouldThrowExceptionWhenFindingMissingMouse() {
        val id = 999L

        whenever(mouseRepository.findById(id))
            .thenReturn(Optional.empty())

        assertThatThrownBy {
            mouseService.findById(id)
        }
            .isInstanceOf(NoSuchElementException::class.java)
            .hasMessageContaining("999")

        verify(mouseRepository).findById(id)
    }

    @Test
    fun shouldCreateMouse() {
        mockStrainId()

        val request = MouseRequest(
            animalNumber = 1001,
            sex = Mouse.Sex.M,
            strainId = 10L,
            transgenicLineId = null,
            labProcedureId = null,
            motherId = null,
            fatherId = null,
            birthDate = LocalDate.of(2026, 1, 10),
            deathDate = null,
            room = "A01",
            rack = "R01",
            cage = "C01",
            origin = "Prague",
            note = "Test mouse"
        )

        whenever(
            mouseRepository.existsByAnimalNumber(1001)
        ).thenReturn(false)

        whenever(
            strainRepository.findById(10L)
        ).thenReturn(Optional.of(strain))

        whenever(
            mouseRepository.save(any<Mouse>())
        ).thenAnswer { invocation ->
            val mouse =
                invocation.getArgument<Mouse>(0)

            ReflectionTestUtils.setField(
                mouse,
                "id",
                1L
            )

            mouse
        }

        val result =
            mouseService.create(request)

        assertThat(result.id)
            .isEqualTo(1L)

        assertThat(result.animalNumber)
            .isEqualTo(1001)

        assertThat(result.sex)
            .isEqualTo(Mouse.Sex.M)

        assertThat(result.strainId)
            .isEqualTo(10L)

        assertThat(result.birthDate)
            .isEqualTo(
                LocalDate.of(2026, 1, 10)
            )

        assertThat(result.deathDate)
            .isNull()

        assertThat(result.room)
            .isEqualTo("A01")

        verify(mouseRepository)
            .existsByAnimalNumber(1001)

        verify(strainRepository)
            .findById(10L)

        verify(mouseRepository)
            .save(any<Mouse>())
    }

    @Test
    fun shouldThrowExceptionWhenCreatingMouseWithDuplicateAnimalNumber() {
        val request = createRequest(
            animalNumber = 1001,
            birthDate = LocalDate.of(2026, 1, 10),
            deathDate = null
        )

        whenever(
            mouseRepository.existsByAnimalNumber(1001)
        ).thenReturn(true)

        assertThatThrownBy {
            mouseService.create(request)
        }
            .isInstanceOf(
                DuplicateMouseAnimalNumberException::class.java
            )
            .hasMessageContaining("1001")

        verify(mouseRepository)
            .existsByAnimalNumber(1001)

        verify(mouseRepository, never())
            .save(any<Mouse>())

        verify(strainRepository, never())
            .findById(any())
    }

    @Test
    fun shouldThrowExceptionWhenDeathDateIsBeforeBirthDate() {
        val birthDate =
            LocalDate.of(2026, 3, 10)

        val deathDate =
            LocalDate.of(2026, 3, 9)

        val request = createRequest(
            animalNumber = 1001,
            birthDate = birthDate,
            deathDate = deathDate
        )

        whenever(
            mouseRepository.existsByAnimalNumber(1001)
        ).thenReturn(false)

        assertThatThrownBy {
            mouseService.create(request)
        }
            .isInstanceOf(
                IllegalArgumentException::class.java
            )
            .hasMessage(
                "Death date cannot be before birth date"
            )

        verify(mouseRepository, never())
            .save(any<Mouse>())

        verify(strainRepository, never())
            .findById(any())
    }

    @Test
    fun shouldAllowDeathDateEqualToBirthDate() {
        val date =
            LocalDate.of(2026, 3, 10)

        val request = createRequest(
            animalNumber = 1001,
            birthDate = date,
            deathDate = date
        )

        whenever(
            mouseRepository.existsByAnimalNumber(1001)
        ).thenReturn(false)

        whenever(
            strainRepository.findById(10L)
        ).thenReturn(Optional.of(strain))

        whenever(
            mouseRepository.save(any<Mouse>())
        ).thenAnswer { invocation ->
            val mouse =
                invocation.getArgument<Mouse>(0)

            ReflectionTestUtils.setField(
                mouse,
                "id",
                1L
            )

            mouse
        }

        val result =
            mouseService.create(request)

        assertThat(result.birthDate)
            .isEqualTo(date)

        assertThat(result.deathDate)
            .isEqualTo(date)

        verify(mouseRepository)
            .save(any<Mouse>())
    }

    @Test
    fun shouldCreateMouseWithTransgenicLine() {//AndLabProcedure() {
        val request = MouseRequest(
            animalNumber = 1001,
            sex = Mouse.Sex.F,
            strainId = 10L,
            transgenicLineId = 20L,
        //     labProcedureId = 30L,
            motherId = null,
            fatherId = null,
            birthDate = LocalDate.of(2026, 1, 10),
            deathDate = null,
            room = "A01",
            rack = "R01",
            cage = "C01",
            origin = null,
            note = null
        )

        whenever(
            mouseRepository.existsByAnimalNumber(1001)
        ).thenReturn(false)

        whenever(
            strainRepository.findById(10L)
        ).thenReturn(Optional.of(strain))

        whenever(
            transgenicLineRepository.findById(20L)
        ).thenReturn(
            Optional.of(transgenicLine)
        )

        // whenever(
        //     labProcedureRepository.findById(30L)
        // ).thenReturn(
        //     Optional.of(labProcedure)
        // )

        whenever(transgenicLine.id)
            .thenReturn(20L)

        // whenever(labProcedure.id)
        //     .thenReturn(30L)

        whenever(
            mouseRepository.save(any<Mouse>())
        ).thenAnswer { invocation ->
            val mouse =
                invocation.getArgument<Mouse>(0)

            ReflectionTestUtils.setField(
                mouse,
                "id",
                1L
            )

            mouse
        }

        val result =
            mouseService.create(request)

        assertThat(result.transgenicLineId)
            .isEqualTo(20L)

        // assertThat(result.labProcedureId)
        //     .isEqualTo(30L)

        verify(transgenicLineRepository)
            .findById(20L)

        // verify(labProcedureRepository)
        //     .findById(30L)
    }

    @Test
    fun shouldUpdateExistingMouse() {
        val id = 13L

        val mouse = createMouse(
            id = id,
            animalNumber = 1001,
            sex = Mouse.Sex.M
        )

        val request = MouseRequest(
            animalNumber = 2001,
            sex = Mouse.Sex.F,
            strainId = 10L,
            transgenicLineId = null,
            labProcedureId = null,
            motherId = null,
            fatherId = null,
            birthDate = LocalDate.of(2026, 2, 1),
            deathDate = LocalDate.of(2026, 5, 1),
            room = "A02",
            rack = "R02",
            cage = "C02",
            origin = "Berlin",
            note = "Updated"
        )

        whenever(
            mouseRepository.findById(id)
        ).thenReturn(Optional.of(mouse))

        whenever(
            mouseRepository.existsByAnimalNumberAndIdNot(
                2001,
                id
            )
        ).thenReturn(false)

        whenever(
            strainRepository.findById(10L)
        ).thenReturn(Optional.of(strain))

        val result =
            mouseService.update(
                id,
                request
            )

        assertThat(result.id)
            .isEqualTo(id)

        assertThat(result.animalNumber)
            .isEqualTo(2001)

        assertThat(result.sex)
            .isEqualTo(Mouse.Sex.F)

        assertThat(result.birthDate)
            .isEqualTo(
                LocalDate.of(2026, 2, 1)
            )

        assertThat(result.deathDate)
            .isEqualTo(
                LocalDate.of(2026, 5, 1)
            )

        assertThat(result.room)
            .isEqualTo("A02")

        assertThat(result.rack)
            .isEqualTo("R02")

        assertThat(result.cage)
            .isEqualTo("C02")

        assertThat(result.origin)
            .isEqualTo("Berlin")

        assertThat(result.note)
            .isEqualTo("Updated")

        assertThat(mouse.animalNumber)
            .isEqualTo(2001)

        assertThat(mouse.sex)
            .isEqualTo(Mouse.Sex.F)

        verify(mouseRepository)
            .findById(id)

        verify(mouseRepository)
            .existsByAnimalNumberAndIdNot(
                2001,
                id
            )
    }

    @Test
    fun shouldThrowExceptionWhenUpdatingMouseWithDuplicateAnimalNumber() {
        val id = 1L

        val mouse = createMouse(
            id = id,
            animalNumber = 1001,
            sex = Mouse.Sex.M
        )

        val request = createRequest(
            animalNumber = 2001,
            birthDate = LocalDate.of(2026, 1, 10),
            deathDate = null
        )

        whenever(
            mouseRepository.findById(id)
        ).thenReturn(Optional.of(mouse))

        whenever(
            mouseRepository.existsByAnimalNumberAndIdNot(
                2001,
                id
            )
        ).thenReturn(true)

        assertThatThrownBy {
            mouseService.update(
                id,
                request
            )
        }
            .isInstanceOf(
                DuplicateMouseAnimalNumberException::class.java
            )
            .hasMessageContaining("2001")

        assertThat(mouse.animalNumber)
            .isEqualTo(1001)

        assertThat(mouse.sex)
            .isEqualTo(Mouse.Sex.M)

        verify(mouseRepository)
            .findById(id)

        verify(mouseRepository)
            .existsByAnimalNumberAndIdNot(
                2001,
                id
            )

        verify(strainRepository, never())
            .findById(any())
    }

    @Test
    fun shouldThrowExceptionWhenUpdatingMissingMouse() {
        val id = 999L

        val request = createRequest(
            animalNumber = 2001,
            birthDate = LocalDate.of(2026, 1, 10),
            deathDate = null
        )

        whenever(
            mouseRepository.findById(id)
        ).thenReturn(Optional.empty())

        assertThatThrownBy {
            mouseService.update(
                id,
                request
            )
        }
            .isInstanceOf(
                NoSuchElementException::class.java
            )
            .hasMessageContaining("999")

        verify(mouseRepository)
            .findById(id)

        verify(mouseRepository, never())
            .existsByAnimalNumberAndIdNot(
                any(),
                any()
            )
    }

    @Test
    fun shouldDeleteExistingMouse() {
        val id = 13L

        val mouse = createMouse(
            id = id,
            animalNumber = 1001,
            sex = Mouse.Sex.M
        )

        whenever(
            mouseRepository.findById(id)
        ).thenReturn(Optional.of(mouse))

        mouseService.delete(id)

        verify(mouseRepository)
            .findById(id)

        verify(mouseRepository)
            .delete(mouse)
    }

    @Test
    fun shouldThrowExceptionWhenDeletingMissingMouse() {
        val id = 999L

        whenever(
            mouseRepository.findById(id)
        ).thenReturn(Optional.empty())

        assertThatThrownBy {
            mouseService.delete(id)
        }
            .isInstanceOf(
                NoSuchElementException::class.java
            )
            .hasMessageContaining("999")

        verify(mouseRepository)
            .findById(id)

        verify(mouseRepository, never())
            .delete(any<Mouse>())
    }

    private fun createMouse(
        id: Long,
        animalNumber: Int,
        sex: Mouse.Sex
    ): Mouse {
        val mouse = Mouse(
            animalNumber = animalNumber,
            sex = sex,
            strain = strain,
            transgenicLine = null,
            labProcedure = null,
            mother = null,
            father = null,
            birthDate = LocalDate.of(2026, 1, 10),
            deathDate = null,
            room = "A01",
            rack = "R01",
            cage = "C01",
            origin = "Prague",
            note = null
        )

        ReflectionTestUtils.setField(
            mouse,
            "id",
            id
        )

        return mouse
    }

    private fun createRequest(
        animalNumber: Int,
        birthDate: LocalDate?,
        deathDate: LocalDate?
    ): MouseRequest =
        MouseRequest(
            animalNumber = animalNumber,
            sex = Mouse.Sex.M,
            strainId = 10L,
            transgenicLineId = null,
            labProcedureId = null,
            motherId = null,
            fatherId = null,
            birthDate = birthDate,
            deathDate = deathDate,
            room = "A01",
            rack = "R01",
            cage = "C01",
            origin = "Prague",
            note = null
        )
}