package cz.animalhouse.service

import cz.animalhouse.dto.MouseRequest
import cz.animalhouse.dto.MouseResponse
import cz.animalhouse.entity.LabProcedure
import cz.animalhouse.entity.Mouse
import cz.animalhouse.entity.Strain
import cz.animalhouse.entity.TransgenicLine
import cz.animalhouse.exception.DuplicateMouseAnimalNumberException
import cz.animalhouse.exception.MouseSexChangeInUseException
// import cz.animalhouse.repository.LabProcedureRepository
import cz.animalhouse.repository.MouseRepository
import cz.animalhouse.repository.StrainRepository
import cz.animalhouse.repository.TransgenicLineRepository
import cz.animalhouse.testutil.TestClock
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argThat
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.not
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.test.util.ReflectionTestUtils
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class MouseServiceTest {

    private val FIXED_TODAY = TestClock.FIXED_TODAY
    private val clock = TestClock.clock

    @Mock
    private lateinit var mouseRepository: MouseRepository

    @Mock
    private lateinit var strainRepository: StrainRepository

    @Mock
    private lateinit var transgenicLineRepository: TransgenicLineRepository

//     @Mock
//     private lateinit var labProcedureRepository: LabProcedureRepository

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
            transgenicLineRepository,
            //labProcedureRepository,
            clock
        )

        strain = Strain(
            "Strain Code",
            "A Name of the Strain"
        )

        ReflectionTestUtils.setField(
            strain,
            "id",
            10L
        )
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
        val PAGE_SIZE = 10
        val FAKE_TOTAL_ITEMS_IN_DB = 231L
        val FAKE_TOTAL_PAGES = FAKE_TOTAL_ITEMS_IN_DB / PAGE_SIZE + 1
        val pageable = PageRequest.of(0, PAGE_SIZE)

        whenever(mouseRepository.findAll(pageable))
            .thenReturn(
                PageImpl(
                    listOf(mouse1, mouse2),
                    pageable,
                    FAKE_TOTAL_ITEMS_IN_DB
                )
            )

        val result: Page<MouseResponse> =
            mouseService.findAll(pageable)

        assertThat(result.content).hasSize(2)

        assertThat(result.content.map { it.animalNumber })
            .containsExactly(
                1001,
                1002
            )

        assertThat(result.content.map { it.sex })
            .containsExactly(
                Mouse.Sex.M,
                Mouse.Sex.F
            )

        assertThat(result.totalElements).isEqualTo(FAKE_TOTAL_ITEMS_IN_DB)
        assertThat(result.totalPages).isEqualTo(FAKE_TOTAL_PAGES)

        verify(mouseRepository).findAll(pageable)
    }

    @Test
    fun shouldFindMouseById() {
        val id = 13L

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
    fun shouldFindNextAnimalNumber() {
        whenever(mouseRepository.findNextAnimalNumber())
            .thenReturn(13)

        val result = mouseService.findNextAnimalNumber()

        assertThat(result).isEqualTo(13)

        verify(mouseRepository).findNextAnimalNumber()
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

        val request = MouseRequest(
            animalNumber = 1001,
            sex = Mouse.Sex.M,
            strainId = 10L,
            transgenicLineId = null,
            labProcedureId = null,
            motherAnimalNumber = null,
            fatherAnimalNumber = null,
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
            motherAnimalNumber = null,
            fatherAnimalNumber = null,
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
    fun `create should assign mother by animal number`() {
        val mother = createMouse(
            id = 1L,
            animalNumber = 26010101,
            sex = Mouse.Sex.F
        )

        whenever(
            mouseRepository.findByAnimalNumber(26010101)
        ).thenReturn(mother)

        whenever(
            strainRepository.findById(10L)
        ).thenReturn(Optional.of(strain))

        whenever(
            mouseRepository.save(any<Mouse>())
        ).thenAnswer { val mouse = it.arguments[0] as Mouse
            ReflectionTestUtils.setField(
                mouse,
                "id",
                13L
            )
            mouse
        }

        val request = createRequest(
            animalNumber = 76020101,
            motherAnimalNumber = 26010101
        )

        mouseService.create(request)

        verify(mouseRepository)
            .findByAnimalNumber(26010101)

        verify(mouseRepository).save(
            argThat { mouse ->
                mouse.mother === mother
            }
        )
    }

    @Test
    fun `create should throw exception when mother animal number does not exist`() {
        whenever(
            mouseRepository.findByAnimalNumber(26010101)
        ).thenReturn(null)

        whenever(
            strainRepository.findById(10L)
        ).thenReturn(Optional.of(strain))

        val request = createRequest(
            animalNumber = 36020101,
            motherAnimalNumber = 26010101
        )

        assertThatThrownBy {
            mouseService.create(request)
        }
            .isInstanceOf(
                IllegalArgumentException::class.java
            )
            .hasMessage(
                "Mouse with animal number 26010101 does not exist."
            )

        verify(mouseRepository, never()).save(any())
    }

    @Test
    fun `create should not search for mother when mother animal number is null`() {
        val request = createRequest(
            animalNumber = 130001,
            motherAnimalNumber = null
        )

        whenever(
            strainRepository.findById(10L)
        ).thenReturn(Optional.of(strain))

        whenever(
            mouseRepository.save(any<Mouse>())
        ).thenAnswer { val mouse = it.arguments[0] as Mouse
            ReflectionTestUtils.setField(
                mouse,
                "id",
                1L
            )
            mouse
        }

        mouseService.create(request)

        verify(
            mouseRepository,
            never()
        ).findByAnimalNumber(any())
    }

    @Test
    fun `create should assign father by animal number`() {
        val father = createMouse(
            id = 7L,
            animalNumber = 26010101,
            sex = Mouse.Sex.M
        )

        whenever(
            mouseRepository.findByAnimalNumber(26010101)
        ).thenReturn(father)

        whenever(
            strainRepository.findById(10L)
        ).thenReturn(Optional.of(strain))

        whenever(
            mouseRepository.save(any<Mouse>())
        ).thenAnswer { val mouse = it.arguments[0] as Mouse
            ReflectionTestUtils.setField(
                mouse,
                "id",
                13L
            )
            mouse
        }

        val request = createRequest(
            animalNumber = 76020101,
            fatherAnimalNumber = 26010101
        )

        mouseService.create(request)

        verify(mouseRepository)
            .findByAnimalNumber(26010101)

        verify(mouseRepository).save(
            argThat { mouse ->
                mouse.father === father
            }
        )
    }
    @Test
    fun `create should reject mouse as its own mother`() {
        // request.animalNumber == motherAnimalNumber

        val request = createRequest(
            animalNumber = 26010101,
            motherAnimalNumber = 26010101
        )

        assertThatThrownBy {
            mouseService.create(request)
        }
            .isInstanceOf(
                IllegalArgumentException::class.java
            )
            .hasMessage(
                "Mouse cannot be its own mother."
            )
    }
    @Test
    fun `create should reject mouse as its own father`() {
        // request.animalNumber == fatherAnimalNumber

        val request = createRequest(
            animalNumber = 91,
            fatherAnimalNumber = 91
        )

        assertThatThrownBy {
            mouseService.create(request)
        }
            .isInstanceOf(
                IllegalArgumentException::class.java
            )
            .hasMessage(
                "Mouse cannot be its own father."
            )

    }
    @Test
    fun `create should reject same mouse as mother and father`() {

        val request = createRequest(
            animalNumber = 29,
            motherAnimalNumber = 13,
            fatherAnimalNumber = 13
        )

        assertThatThrownBy {
            mouseService.create(request)
        }
            .isInstanceOf(
                IllegalArgumentException::class.java
            )
            .hasMessage(
                "Mother and father must be different mice."
            )

        verify(mouseRepository, never())
            .findByAnimalNumber(13)
    }
    @Test
    fun `create should reject male mouse as mother`() {
        val male = createMouse(
            id = 8L,
            animalNumber = 13,
            sex = Mouse.Sex.M
        )

        whenever(
            mouseRepository.findByAnimalNumber(13)
        ).thenReturn(male)

        whenever(
            strainRepository.findById(10L)
        ).thenReturn(Optional.of(strain))

        val request = createRequest(
            animalNumber = 29,
            motherAnimalNumber = 13
        )

        assertThatThrownBy {
            mouseService.create(request)
        }
            .isInstanceOf(
                IllegalArgumentException::class.java
            )
            .hasMessage(
                "Mother must be female."
            )

        verify(mouseRepository)
            .findByAnimalNumber(13)
    }
    @Test
    fun `create should reject female mouse as father`() {
        val female = createMouse(
            id = 1L,
            animalNumber = 71,
            sex = Mouse.Sex.F
        )

        whenever(
            mouseRepository.findByAnimalNumber(71)
        ).thenReturn(female)

        whenever(
            strainRepository.findById(10L)
        ).thenReturn(Optional.of(strain))

        val request = createRequest(
            animalNumber = 26010101,
            fatherAnimalNumber = 71
        )

        assertThatThrownBy {
            mouseService.create(request)
        }
            .isInstanceOf(
                IllegalArgumentException::class.java
            )
            .hasMessage(
                "Father must be male."
            )

        verify(mouseRepository)
            .findByAnimalNumber(71)

    }

    @Test
    fun `create should reject birth date in the future`() {
        val request = createRequest(
            animalNumber = 13,
            birthDate = FIXED_TODAY.plusDays(1)
        )

        assertThatThrownBy {
            mouseService.create(request)
        }
            .isInstanceOf(
                IllegalArgumentException::class.java
            )
            .hasMessage(
                "Birth date cannot be in the future."
            )
    }
    @Test
    fun `create should reject death date in the future`() {
        val request = createRequest(
            animalNumber = 13,
            deathDate = FIXED_TODAY.plusDays(1)
        )

        assertThatThrownBy {
            mouseService.create(request)
        }
            .isInstanceOf(
                IllegalArgumentException::class.java
            )
            .hasMessage(
                "Death date cannot be in the future."
            )
    }

    @Test
    fun `update should assign mother by animal number`() {
        val UPDATE_ID = 13L
        val MOUSE_ANIMAL_NUMBER = 1017
        val MOTHER_ANIMAL_NUMBER = 137

        val mouse = createMouse(
            id = UPDATE_ID,
            animalNumber = MOUSE_ANIMAL_NUMBER,
            sex = Mouse.Sex.M
        )
        val mother = createMouse(
            id = 7L,
            animalNumber = MOTHER_ANIMAL_NUMBER,
            sex = Mouse.Sex.F
        )

        val request = MouseRequest(
            animalNumber = MOUSE_ANIMAL_NUMBER,
            sex = Mouse.Sex.F,
            strainId = 10L,

            motherAnimalNumber = MOTHER_ANIMAL_NUMBER
        )

        whenever(
            mouseRepository.findById(UPDATE_ID)
        ).thenReturn(Optional.of(mouse))

        whenever(
            mouseRepository.findByAnimalNumber(MOTHER_ANIMAL_NUMBER)
        ).thenReturn(mother)

        whenever(
            mouseRepository.existsByAnimalNumberAndIdNot(
                MOUSE_ANIMAL_NUMBER,
                UPDATE_ID
            )
        ).thenReturn(false)

        whenever(
            strainRepository.findById(10L)
        ).thenReturn(Optional.of(strain))

        val result =
            mouseService.update(
                UPDATE_ID,
                request
            )

        assertThat(result.id)
            .isEqualTo(UPDATE_ID)

        assertThat(result.animalNumber)
            .isEqualTo(MOUSE_ANIMAL_NUMBER)

        assertThat(result.motherAnimalNumber)
            .isEqualTo(MOTHER_ANIMAL_NUMBER)

        verify(mouseRepository)
            .findById(UPDATE_ID)

        verify(mouseRepository)
            .existsByAnimalNumberAndIdNot(
                MOUSE_ANIMAL_NUMBER,
                UPDATE_ID
            )

        verify(mouseRepository)
            .findByAnimalNumber(MOTHER_ANIMAL_NUMBER)
    }

    @Test
    fun `update should throw exception when mother animal number does not exist`() {
        val MOTHER_ANIMAL_NUMBER = 137
        val UPDATE_ID = 13L
        val MOUSE_ANIMAL_NUMBER = 1017

        val mouse = createMouse(
            id = UPDATE_ID,
            animalNumber = MOUSE_ANIMAL_NUMBER,
            sex = Mouse.Sex.M
        )

        whenever(
            mouseRepository.findById(UPDATE_ID)
        ).thenReturn(Optional.of(mouse))

        whenever(
            mouseRepository.existsByAnimalNumberAndIdNot(
                MOUSE_ANIMAL_NUMBER,
                UPDATE_ID
            )
        ).thenReturn(false)

        whenever(
            mouseRepository.findByAnimalNumber(MOTHER_ANIMAL_NUMBER)
        ).thenReturn(null)

        whenever(
            strainRepository.findById(10L)
        ).thenReturn(Optional.of(strain))


        val request = createRequest(
            animalNumber = MOUSE_ANIMAL_NUMBER,
            motherAnimalNumber = MOTHER_ANIMAL_NUMBER
        )

        assertThatThrownBy {
            mouseService.update(
                UPDATE_ID,
                request
            )
        }
            .isInstanceOf(
                IllegalArgumentException::class.java
            )
            .hasMessage(
                "Mouse with animal number $MOTHER_ANIMAL_NUMBER does not exist."
            )
    }

    @Test
    fun `update should not search for father when its animal number is null`() {
        val MOTHER_ANIMAL_NUMBER = 137
        val UPDATE_ID = 13L
        val MOUSE_ANIMAL_NUMBER = 1017
        val NULL: Int? = null

        val mother = createMouse(
            id = 7L,
            animalNumber = MOTHER_ANIMAL_NUMBER,
            sex = Mouse.Sex.F
        )
        val mouse = createMouse(
            id = UPDATE_ID,
            animalNumber = MOUSE_ANIMAL_NUMBER,
            sex = Mouse.Sex.M
        )
        whenever(
            mouseRepository.findById(UPDATE_ID)
        ).thenReturn(Optional.of(mouse))

        whenever(
            mouseRepository.existsByAnimalNumberAndIdNot(
                MOUSE_ANIMAL_NUMBER,
                UPDATE_ID
            )
        ).thenReturn(false)

        whenever(
            mouseRepository.findByAnimalNumber(eq(MOTHER_ANIMAL_NUMBER))
        ).thenReturn(mother)

        whenever(
            strainRepository.findById(10L)
        ).thenReturn(Optional.of(strain))


        val request = createRequest(
            animalNumber = MOUSE_ANIMAL_NUMBER,
            motherAnimalNumber = MOTHER_ANIMAL_NUMBER,
            fatherAnimalNumber = NULL
        )

        mouseService.update(
            UPDATE_ID,
            request
        )

        verify(
            mouseRepository,
            never()
        ).findByAnimalNumber(not(eq(MOTHER_ANIMAL_NUMBER)))

        verify(
            mouseRepository
        ).findByAnimalNumber(any())
    }

    @Test
    fun `update should assign father by animal number`() {
        val UPDATE_ID = 13L
        val MOUSE_ANIMAL_NUMBER = 1017
        val FATHER_ANIMAL_NUMBER = 137

        val mouse = createMouse(
            id = UPDATE_ID,
            animalNumber = MOUSE_ANIMAL_NUMBER,
            sex = Mouse.Sex.M
        )
        val father = createMouse(
            id = 7L,
            animalNumber = FATHER_ANIMAL_NUMBER,
            sex = Mouse.Sex.M
        )

        val request = MouseRequest(
            animalNumber = MOUSE_ANIMAL_NUMBER,
            sex = Mouse.Sex.F,
            strainId = 10L,

            fatherAnimalNumber = FATHER_ANIMAL_NUMBER
        )

        whenever(
            mouseRepository.findById(UPDATE_ID)
        ).thenReturn(Optional.of(mouse))

        whenever(
            mouseRepository.findByAnimalNumber(FATHER_ANIMAL_NUMBER)
        ).thenReturn(father)

        whenever(
            mouseRepository.existsByAnimalNumberAndIdNot(
                MOUSE_ANIMAL_NUMBER,
                UPDATE_ID
            )
        ).thenReturn(false)

        whenever(
            strainRepository.findById(10L)
        ).thenReturn(Optional.of(strain))

        val result =
            mouseService.update(
                UPDATE_ID,
                request
            )

        assertThat(result.id)
            .isEqualTo(UPDATE_ID)

        assertThat(result.animalNumber)
            .isEqualTo(MOUSE_ANIMAL_NUMBER)

        assertThat(result.fatherAnimalNumber)
            .isEqualTo(FATHER_ANIMAL_NUMBER)

        verify(mouseRepository)
            .findById(UPDATE_ID)

        verify(mouseRepository)
            .existsByAnimalNumberAndIdNot(
                MOUSE_ANIMAL_NUMBER,
                UPDATE_ID
            )

        verify(mouseRepository)
            .findByAnimalNumber(FATHER_ANIMAL_NUMBER)
    }
    @Test
    fun `update should reject when mouse is its own mother`() {
        val UPDATE_ID = 13L
        val MOUSE_ANIMAL_NUMBER = 1017

        val mouse = createMouse(
            id = UPDATE_ID,
            animalNumber = MOUSE_ANIMAL_NUMBER,
            sex = Mouse.Sex.F
        )
        val request = createRequest(
            animalNumber = MOUSE_ANIMAL_NUMBER,
            motherAnimalNumber = MOUSE_ANIMAL_NUMBER
        )

        whenever(
            mouseRepository.findById(UPDATE_ID)
        ).thenReturn(Optional.of(mouse))

        assertThatThrownBy {
            mouseService.update(
                UPDATE_ID,
                request
            )
        }
            .isInstanceOf(
                IllegalArgumentException::class.java
            )
            .hasMessage(
                "Mouse cannot be its own mother."
            )
    }
    @Test
    fun `update should reject when mouse is its own father`() {
        val UPDATE_ID = 13L
        val FATHER_ANIMAL_NUMBER = 1017

        val mouse = createMouse(
            id = UPDATE_ID,
            animalNumber = FATHER_ANIMAL_NUMBER,
            sex = Mouse.Sex.M
        )
        val request = createRequest(
            animalNumber = FATHER_ANIMAL_NUMBER,
            fatherAnimalNumber = FATHER_ANIMAL_NUMBER
        )

        whenever(
            mouseRepository.findById(UPDATE_ID)
        ).thenReturn(Optional.of(mouse))

        assertThatThrownBy {
            mouseService.update(
                UPDATE_ID,
                request
            )
        }
            .isInstanceOf(
                IllegalArgumentException::class.java
            )
            .hasMessage(
                "Mouse cannot be its own father."
            )
    }
    @Test
    fun `update should reject same mouse as mother and father`() {
        val UPDATE_ID = 13L
        val MOUSE_ANIMAL_NUMBER = 227
        val FATHER_ANIMAL_NUMBER = 1017

        val mouse = createMouse(
            id = UPDATE_ID,
            animalNumber = MOUSE_ANIMAL_NUMBER,
            sex = Mouse.Sex.M
        )
        val request = createRequest(
            animalNumber = MOUSE_ANIMAL_NUMBER,
            fatherAnimalNumber = FATHER_ANIMAL_NUMBER,
            motherAnimalNumber = FATHER_ANIMAL_NUMBER
        )

        whenever(
            mouseRepository.findById(UPDATE_ID)
        ).thenReturn(Optional.of(mouse))

        assertThatThrownBy {
            mouseService.update(
                UPDATE_ID,
                request
            )
        }
            .isInstanceOf(
                IllegalArgumentException::class.java
            )
            .hasMessage(
                "Mother and father must be different mice."
            )
    }
    @Test
    fun `update should reject male mouse as mother`() {
        val UPDATE_ID = 13L
        val MOUSE_ANIMAL_NUMBER = 227
        val MALE_ANIMAL_NUMBER = 1017

        val mouse = createMouse(
            id = UPDATE_ID,
            animalNumber = MOUSE_ANIMAL_NUMBER,
            sex = Mouse.Sex.M
        )
        val male = createMouse(
            id = 17709L,
            animalNumber = MALE_ANIMAL_NUMBER,
            sex = Mouse.Sex.M
        )
        val request = createRequest(
            animalNumber = MOUSE_ANIMAL_NUMBER,
            motherAnimalNumber = MALE_ANIMAL_NUMBER
        )

        whenever(
            mouseRepository.findById(UPDATE_ID)
        ).thenReturn(Optional.of(mouse))

        whenever(
            mouseRepository.findByAnimalNumber(MALE_ANIMAL_NUMBER)
        ).thenReturn(male)

        whenever(
            strainRepository.findById(10L)
        ).thenReturn(Optional.of(strain))


        assertThatThrownBy {
            mouseService.update(
                UPDATE_ID,
                request
            )
        }
            .isInstanceOf(
                IllegalArgumentException::class.java
            )
            .hasMessage(
                "Mother must be female."
            )
    }

    @Test
    fun `update should reject female mouse as father`() {
        val UPDATE_ID = 13L
        val MOUSE_ANIMAL_NUMBER = 227
        val FEMALE_ANIMAL_NUMBER = 1017

        val mouse = createMouse(
            id = UPDATE_ID,
            animalNumber = MOUSE_ANIMAL_NUMBER,
            sex = Mouse.Sex.M
        )
        val female = createMouse(
            id = 17709L,
            animalNumber = FEMALE_ANIMAL_NUMBER,
            sex = Mouse.Sex.F
        )
        val request = createRequest(
            animalNumber = MOUSE_ANIMAL_NUMBER,
            fatherAnimalNumber = FEMALE_ANIMAL_NUMBER
        )

        whenever(
            mouseRepository.findById(UPDATE_ID)
        ).thenReturn(Optional.of(mouse))

        whenever(
            mouseRepository.findByAnimalNumber(FEMALE_ANIMAL_NUMBER)
        ).thenReturn(female)

        whenever(
            strainRepository.findById(10L)
        ).thenReturn(Optional.of(strain))


        assertThatThrownBy {
            mouseService.update(
                UPDATE_ID,
                request
            )
        }
            .isInstanceOf(
                IllegalArgumentException::class.java
            )
            .hasMessage(
                "Father must be male."
            )
    }

    @Test
    fun `update should reject birth date in the future`() {
        val UPDATE_ID = 13L
        val MOUSE_ANIMAL_NUMBER = 227


        val mouse = createMouse(
            id = UPDATE_ID,
            animalNumber = MOUSE_ANIMAL_NUMBER,
            sex = Mouse.Sex.M
        )
        val request = createRequest(
            animalNumber = 13,
            birthDate = FIXED_TODAY.plusDays(1)
        )

        whenever(
            mouseRepository.findById(UPDATE_ID)
        ).thenReturn(Optional.of(mouse))

        assertThatThrownBy {
            mouseService.update(UPDATE_ID, request)
        }
            .isInstanceOf(
                IllegalArgumentException::class.java
            )
            .hasMessage(
                "Birth date cannot be in the future."
            )
    }

    @Test
    fun `update should reject death date in the future`() {
        val UPDATE_ID = 17L
        val MOUSE_ANIMAL_NUMBER = 227


        val mouse = createMouse(
            id = UPDATE_ID,
            animalNumber = MOUSE_ANIMAL_NUMBER,
            sex = Mouse.Sex.F
        )
        val request = createRequest(
            animalNumber = 19,
            deathDate = FIXED_TODAY.plusDays(1)
        )

        whenever(
            mouseRepository.findById(UPDATE_ID)
        ).thenReturn(Optional.of(mouse))

        assertThatThrownBy {
            mouseService.update(UPDATE_ID, request)
        }
            .isInstanceOf(
                IllegalArgumentException::class.java
            )
            .hasMessage(
                "Death date cannot be in the future."
            )
    }

    @Test
    fun `update should reject change sex when mouse is a mother`() {
        val UPDATE_ID = 13L
        val MOTHER_ANIMAL_NUMBER = 1017


        val mother = createMouse(
            id = UPDATE_ID,
            animalNumber = MOTHER_ANIMAL_NUMBER,
            sex = Mouse.Sex.F
        )

        whenever(
            mouseRepository.findById(UPDATE_ID)
        ).thenReturn(Optional.of(mother))

        whenever(
            mouseRepository.existsByMotherId(UPDATE_ID)
        ).thenReturn(true)


        val request = createRequest(
            animalNumber = MOTHER_ANIMAL_NUMBER,
            sex = Mouse.Sex.M
        )

        assertThatThrownBy {
            mouseService.update(
                UPDATE_ID,
                request
            )
        }
            .isInstanceOf(
                MouseSexChangeInUseException::class.java
            )
            .hasMessage(
                "Sex of mouse $MOTHER_ANIMAL_NUMBER cannot be changed because the mouse is used as a parent."
            )

        verify(mouseRepository)
            .existsByMotherId(UPDATE_ID)

        verify(mouseRepository, never())
            .existsByFatherId(any())
    }

    @Test
    fun `update should reject change sex when mouse is a father`() {
        val UPDATE_ID = 13L
        val FATHER_ANIMAL_NUMBER = 1017

        val father = createMouse(
            id = UPDATE_ID,
            animalNumber = FATHER_ANIMAL_NUMBER,
            sex = Mouse.Sex.M
        )

        whenever(
            mouseRepository.findById(UPDATE_ID)
        ).thenReturn(Optional.of(father))

        whenever(
            mouseRepository.existsByFatherId(UPDATE_ID)
        ).thenReturn(true)


        val request = createRequest(
            animalNumber = FATHER_ANIMAL_NUMBER,
            sex = Mouse.Sex.F
        )

        assertThatThrownBy {
            mouseService.update(
                UPDATE_ID,
                request
            )
        }
            .isInstanceOf(
                MouseSexChangeInUseException::class.java
            )
            .hasMessage(
                "Sex of mouse $FATHER_ANIMAL_NUMBER cannot be changed because the mouse is used as a parent."
            )

        verify(mouseRepository)
            .existsByFatherId(UPDATE_ID)

        verify(mouseRepository, never())
            .existsByMotherId(any())
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
            motherAnimalNumber = null,
            fatherAnimalNumber = null,
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

    @Test
    fun `findParentCandidates should return parent candidates`() {
        val mouse1 = createMouse(
            id = 17,
            animalNumber = 723001,
            sex = Mouse.Sex.F
        )

        val mouse2 = createMouse(
            id = 19,
            animalNumber = 723002,
            sex = Mouse.Sex.F
        )

        whenever(
            mouseRepository.findParentCandidates(
                infix = eq("723"),
                sex = eq("F"),
                currentDate = any<LocalDate>(),
                femaleMinAgeWeeks = any<Int>(),
                maleMinAgeWeeks = any<Int>()
            )
        ).thenReturn(
            listOf(mouse1, mouse2)
        )

        val result =
            mouseService.findParentCandidates(
                infix = "723",
                sex = "F"
            )

        assertThat(result).hasSize(2)

        assertThat(result)
            .extracting<Int> { it.animalNumber }
            .containsExactly(
                723001,
                723002
            )

        verify(mouseRepository)
            .findParentCandidates(
                infix = eq("723"),
                sex = eq("F"),
                currentDate = any<LocalDate>(),
                femaleMinAgeWeeks = any<Int>(),
                maleMinAgeWeeks = any<Int>()
            )
    }

    @Test
    fun `findParentCandidates should return empty list when repository returns no mice`() {
        whenever(
            mouseRepository.findParentCandidates(
                infix = eq("999"),
                sex = eq("F"),
                currentDate = any<LocalDate>(),
                femaleMinAgeWeeks = any<Int>(),
                maleMinAgeWeeks = any<Int>()
            )
        ).thenReturn(emptyList())

        val result =
            mouseService.findParentCandidates(
                infix = "999",
                sex = "F"
            )

        assertThat(result).isEmpty()

        verify(mouseRepository)
            .findParentCandidates(
                infix = eq("999"),
                sex = eq("F"),
                currentDate = any<LocalDate>(),
                femaleMinAgeWeeks = any<Int>(),
                maleMinAgeWeeks = any<Int>()
            )
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
        sex: Mouse.Sex = Mouse.Sex.M,
        birthDate: LocalDate? = null,
        deathDate: LocalDate? = null,
        motherAnimalNumber: Int? = null,
        fatherAnimalNumber: Int? = null
    ): MouseRequest =
        MouseRequest(
            animalNumber = animalNumber,
            sex = sex,
            strainId = 10L,
            transgenicLineId = null,
            labProcedureId = null,
            motherAnimalNumber = motherAnimalNumber,
            fatherAnimalNumber = fatherAnimalNumber,
            birthDate = birthDate,
            deathDate = deathDate,
            room = "A01",
            rack = "R01",
            cage = "C01",
            origin = "Prague",
            note = null
        )
}