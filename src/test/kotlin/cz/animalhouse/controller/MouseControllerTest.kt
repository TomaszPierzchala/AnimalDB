package cz.animalhouse.controller

import cz.animalhouse.dto.MouseRequest
import cz.animalhouse.dto.MouseResponse
import cz.animalhouse.entity.Mouse
import cz.animalhouse.service.MouseService
import cz.animalhouse.exception.DuplicateMouseAnimalNumberException

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc

import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

import java.time.LocalDate

@WebMvcTest(
    controllers = [MouseController::class],
    properties = [
        "app.cors.allowed-origin=http://localhost:5173"
    ]
)
class MouseControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var mouseService: MouseService

    // -------------------------------------------------
    // GET /api/mice
    // -------------------------------------------------

    @Test
    fun shouldFindAllMice() {

        val mouse1 = createResponse(
            id = 1L,
            animalNumber = 1001,
            sex = Mouse.Sex.M
        )

        val mouse2 = createResponse(
            id = 2L,
            animalNumber = 1002,
            sex = Mouse.Sex.F
        )

        whenever(mouseService.findAll(any<Pageable>()))
            .thenReturn(
                PageImpl(listOf(mouse1, mouse2))
            )

        mockMvc.perform(
            get("/api/mice")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.content[0].id").value(1))
            .andExpect(jsonPath("$.content[0].animalNumber").value(1001))
            .andExpect(jsonPath("$.content[0].sex").value("M"))
            .andExpect(jsonPath("$.content[1].id").value(2))
            .andExpect(jsonPath("$.content[1].animalNumber").value(1002))
            .andExpect(jsonPath("$.content[1].sex").value("F"))

        verify(mouseService).findAll(any<Pageable>())
    }

    // -------------------------------------------------
    // GET /api/mice - pagination and sorting
    // -------------------------------------------------

    @Test
    fun shouldPassPaginationAndSortingToService() {

        whenever(mouseService.findAll(any<Pageable>()))
            .thenReturn(PageImpl(emptyList<MouseResponse>()))

        mockMvc.perform(
            get("/api/mice")
                .param("page", "2")
                .param("size", "10")
                .param("sort", "animalNumber,desc")
        )
            .andExpect(status().isOk)

        val pageableCaptor = argumentCaptor<Pageable>()

        verify(mouseService).findAll(
            pageableCaptor.capture()
        )

        val pageable = pageableCaptor.firstValue

        assertThat(pageable.pageNumber).isEqualTo(2)
        assertThat(pageable.pageSize).isEqualTo(10)

        assertThat(
            pageable.sort.getOrderFor("animalNumber")?.isDescending
        ).isTrue()
    }

    // -------------------------------------------------
    // GET /api/mice/{id}
    // -------------------------------------------------

    @Test
    fun shouldFindMouseById() {

        val mouse = createResponse(
            id = 1L,
            animalNumber = 1001,
            sex = Mouse.Sex.M
        )

        whenever(mouseService.findById(1L))
            .thenReturn(mouse)

        mockMvc.perform(
            get("/api/mice/1")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.animalNumber").value(1001))
            .andExpect(jsonPath("$.sex").value("M"))
            .andExpect(jsonPath("$.strainId").value(10))

        verify(mouseService).findById(1L)
    }

    @Test
    fun shouldReturnNextAnimalNumber() {
        whenever(mouseService.findNextAnimalNumber())
            .thenReturn(13)

        mockMvc.perform(
            get("/api/mice/next-animal-number")
        )
            .andExpect(status().isOk)
            .andExpect(content().string("13"))

        verify(mouseService).findNextAnimalNumber()
    }

    // -------------------------------------------------
    // POST /api/mice
    // -------------------------------------------------

    @Test
    fun shouldCreateMouse() {

        val response = createResponse(
            id = 1L,
            animalNumber = 1001,
            sex = Mouse.Sex.M
        )

        whenever(mouseService.create(any<MouseRequest>()))
            .thenReturn(response)

        mockMvc.perform(
            post("/api/mice")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRequestJson())
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.animalNumber").value(1001))
            .andExpect(jsonPath("$.sex").value("M"))
            .andExpect(jsonPath("$.strainId").value(10))

        val requestCaptor = argumentCaptor<MouseRequest>()

        verify(mouseService).create(
            requestCaptor.capture()
        )

        val request = requestCaptor.firstValue

        assertThat(request.animalNumber).isEqualTo(1001)
        assertThat(request.sex).isEqualTo(Mouse.Sex.M)
        assertThat(request.strainId).isEqualTo(10L)
        assertThat(request.birthDate)
            .isEqualTo(LocalDate.of(2026, 1, 10))
    }

    // -------------------------------------------------
    // PUT /api/mice/{id}
    // -------------------------------------------------

    @Test
    fun shouldUpdateMouse() {

        val response = createResponse(
            id = 1L,
            animalNumber = 2001,
            sex = Mouse.Sex.F
        )

        whenever(
            mouseService.update(
                eq(1L),
                any<MouseRequest>()
            )
        ).thenReturn(response)

        val requestJson = createRequestJson(
            animalNumber = 2001,
            sex = "F"
        )

        mockMvc.perform(
            put("/api/mice/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.animalNumber").value(2001))
            .andExpect(jsonPath("$.sex").value("F"))

        val requestCaptor = argumentCaptor<MouseRequest>()

        verify(mouseService).update(
            eq(1L),
            requestCaptor.capture()
        )

        assertThat(requestCaptor.firstValue.animalNumber)
            .isEqualTo(2001)

        assertThat(requestCaptor.firstValue.sex)
            .isEqualTo(Mouse.Sex.F)
    }

    // -------------------------------------------------
    // DELETE /api/mice/{id}
    // -------------------------------------------------

    @Test
    fun shouldDeleteMouse() {

        mockMvc.perform(
            delete("/api/mice/1")
        )
            .andExpect(status().isNoContent)
            .andExpect(content().string(""))

        verify(mouseService).delete(1L)
    }

    @Test
    fun shouldReturn404WhenFindingMissingMouse() {

        whenever(mouseService.findById(999L))
            .thenThrow(
                NoSuchElementException(
                    "Mouse with id=999 not found"
                )
            )

        mockMvc.perform(
            get("/api/mice/999")
        )
            .andExpect(status().isNotFound)

        verify(mouseService).findById(999L)
    }

    @Test
    fun shouldReturn404WhenUpdatingMissingMouse() {

        whenever(
            mouseService.update(
                eq(999L),
                any<MouseRequest>()
            )
        ).thenThrow(
            NoSuchElementException(
                "Mouse with id=999 not found"
            )
        )

        mockMvc.perform(
            put("/api/mice/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRequestJson())
        )
            .andExpect(status().isNotFound)

        verify(mouseService).update(
            eq(999L),
            any<MouseRequest>()
        )
    }

    @Test
    fun shouldReturn404WhenDeletingMissingMouse() {

        doThrow(
            NoSuchElementException(
                "Mouse with id=999 not found"
            )
        ).whenever(mouseService).delete(999L)

        mockMvc.perform(
            delete("/api/mice/999")
        )
            .andExpect(status().isNotFound)

        verify(mouseService).delete(999L)
    }

    @Test
    fun shouldReturn409WhenCreatingMouseWithDuplicateAnimalNumber() {

        whenever(
            mouseService.create(any<MouseRequest>())
        ).thenThrow(
            DuplicateMouseAnimalNumberException(1001)
        )

        mockMvc.perform(
            post("/api/mice")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRequestJson())
        )
            .andExpect(status().isConflict)

        verify(mouseService).create(any<MouseRequest>())
    }

    @Test
    fun shouldReturn409WhenUpdatingMouseWithDuplicateAnimalNumber() {

        whenever(
            mouseService.update(
                eq(1L),
                any<MouseRequest>()
            )
        ).thenThrow(
            DuplicateMouseAnimalNumberException(2001)
        )

        mockMvc.perform(
            put("/api/mice/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    createRequestJson(animalNumber = 2001)
                )
        )
            .andExpect(status().isConflict)

        verify(mouseService).update(
            eq(1L),
            any<MouseRequest>()
        )
    }

    @Test
    fun shouldReturn400WhenDeathDateIsBeforeBirthDate() {

        whenever(
            mouseService.create(any<MouseRequest>())
        ).thenThrow(
            IllegalArgumentException(
                "Death date cannot be before birth date"
            )
        )

        val requestJson = """
        {
            "animalNumber": 1001,
            "sex": "M",
            "strainId": 10,
            "birthDate": "2026-01-10",
            "deathDate": "2026-01-09"
        }
    """.trimIndent()

        mockMvc.perform(
            post("/api/mice")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
        )
            .andExpect(status().isBadRequest)

        verify(mouseService).create(any<MouseRequest>())
    }

    @Test
    fun shouldReturn400WhenSexIsInvalid() {

        val requestJson = """
        {
            "animalNumber": 1001,
            "sex": "X",
            "strainId": 10
        }
    """.trimIndent()

        mockMvc.perform(
            post("/api/mice")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
        )
            .andExpect(status().isBadRequest)

        verify(
            mouseService,
            org.mockito.kotlin.never()
        ).create(any<MouseRequest>())
    }

    @Test
    fun shouldReturn400WhenAnimalNumberIsNegative() {

        val requestJson = createRequestJson(
            animalNumber = -1
        )

        mockMvc.perform(
            post("/api/mice")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
        )
            .andExpect(status().isBadRequest)

        verify(
            mouseService,
            org.mockito.kotlin.never()
        ).create(any<MouseRequest>())
    }

    // -------------------------------------------------
    // Helpers
    // -------------------------------------------------

    private fun createResponse(
        id: Long,
        animalNumber: Int,
        sex: Mouse.Sex
    ): MouseResponse =
        MouseResponse(
            id = id,
            animalNumber = animalNumber,
            sex = sex,
            strainId = 10L,
            strainCode = "STR1",
            strainName = "FIRST strain",
            transgenicLineId = null,
            labProcedureId = null,
            motherId = null,
            fatherId = null,
            birthDate = LocalDate.of(2026, 1, 10),
            deathDate = null,
            room = "A1",
            rack = "R2",
            cage = "C5",
            origin = "Prague",
            note = null
        )

    private fun createRequestJson(
        animalNumber: Int = 1001,
        sex: String = "M"
    ): String =
        """
        {
            "animalNumber": $animalNumber,
            "sex": "$sex",
            "strainId": 10,
            "birthDate": "2026-01-10",
            "room": "A1",
            "rack": "R2",
            "cage": "C5",
            "origin": "Prague"
        }
        """.trimIndent()
}