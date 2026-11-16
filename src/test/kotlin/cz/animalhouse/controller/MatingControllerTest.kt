package cz.animalhouse.controller

import cz.animalhouse.dto.MatingRequest
import cz.animalhouse.dto.MatingResponse
import cz.animalhouse.service.MatingService

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
import kotlin.Long

@WebMvcTest(
    controllers = [MatingController::class],
    properties = [
        "app.cors.allowed-origin=http://localhost:5173"
    ]
)
class MatingControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var matingService: MatingService

    // -------------------------------------------------
    // GET /api/matings
    // -------------------------------------------------

    @Test
    fun shouldFindAllMatings() {

        val mating1 = createResponse(
            id = 1L,
            femaleAnimalNumber = 3,
            maleAnimalNumber = 7
        )
        val mating2 = createResponse(
            id = 2L,
            femaleAnimalNumber = 17,
            maleAnimalNumber = 19
        )

        whenever(matingService.findAll(any<Pageable>()))
            .thenReturn(
                PageImpl(listOf(mating1, mating2))
            )

        mockMvc.perform(
            get("/api/matings")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.content[0].id").value(1))
            .andExpect(jsonPath("$.content[0].femaleAnimalNumber").value(3))
            .andExpect(jsonPath("$.content[0].maleAnimalNumber").value(7))
            .andExpect(jsonPath("$.content[0].matingDate").value("2026-10-21"))
            .andExpect(jsonPath("$.content[0].bornCount").value(10))
            .andExpect(jsonPath("$.content[0].aliveCount").value(8))
            .andExpect(jsonPath("$.content[0].femaleCount").value(5))
            .andExpect(jsonPath("$.content[0].maleCount").value(3))
            .andExpect(jsonPath("$.content[0].note").value("Some note"))

            .andExpect(jsonPath("$.content[1].id").value(2))
            .andExpect(jsonPath("$.content[1].femaleAnimalNumber").value(17))
            .andExpect(jsonPath("$.content[1].maleAnimalNumber").value(19))


        verify(matingService).findAll(any<Pageable>())
    }

    // -------------------------------------------------
    // GET /api/matings - pagination and sorting
    // -------------------------------------------------

    @Test
    fun shouldPassPaginationAndSortingToService() {

        whenever(matingService.findAll(any<Pageable>()))
            .thenReturn(PageImpl(emptyList()))

        mockMvc.perform(
            get("/api/matings")
                .param("page", "3")
                .param("size", "17")
                .param("sort", "maleAnimalNumber,desc")
        )
            .andExpect(status().isOk)

        val pageableCaptor = argumentCaptor<Pageable>()

        verify(matingService).findAll(
            pageableCaptor.capture()
        )

        val pageable = pageableCaptor.firstValue

        assertThat(pageable.pageNumber).isEqualTo(3)
        assertThat(pageable.pageSize).isEqualTo(17)

        assertThat(
            pageable.sort.getOrderFor("maleAnimalNumber")?.isDescending
        ).isTrue()
    }

    // -------------------------------------------------
    // GET /api/matings/{id}
    // -------------------------------------------------

    @Test
    fun shouldGetMatingById() {

        val mating = createResponse(
            id = 1L,
            femaleAnimalNumber = 1001,
            maleAnimalNumber = 2003
        )

        whenever(matingService.findById(1L))
            .thenReturn(mating)

        mockMvc.perform(
            get("/api/matings/1")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.femaleAnimalNumber").value(1001))
            .andExpect(jsonPath("$.maleAnimalNumber").value(2003))
            .andExpect(jsonPath("$.aliveCount").value(8))
            .andExpect(jsonPath("$.note").value("Some note"))

        verify(matingService).findById(1L)
    }
    
    // -------------------------------------------------
    // POST /api/matings
    // -------------------------------------------------

    @Test
    fun shouldCreateMating() {

        val response = createResponse(
            id = 1L,
            femaleAnimalNumber = 1001,
            maleAnimalNumber = 703
        )

        whenever(matingService.create(any<MatingRequest>()))
            .thenReturn(response)

        mockMvc.perform(
            post("/api/matings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRequestJson(femaleAnimalNumber = 1001, maleAnimalNumber = 703))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.femaleAnimalNumber").value(1001))
            .andExpect(jsonPath("$.maleAnimalNumber").value(703))
            .andExpect(jsonPath("$.matingDate").value("2026-10-21"))
            .andExpect(jsonPath("$.bornCount").value(10))
            .andExpect(jsonPath("$.aliveCount").value(8))
            .andExpect(jsonPath("$.femaleCount").value(5))
            .andExpect(jsonPath("$.maleCount").value(3))
            .andExpect(jsonPath("$.note").value("Some note"))

        val requestCaptor = argumentCaptor<MatingRequest>()

        verify(matingService).create(
            requestCaptor.capture()
        )

        val request = requestCaptor.firstValue

        assertThat(request.femaleAnimalNumber).isEqualTo(1001)
        assertThat(request.maleAnimalNumber).isEqualTo(703)

        assertThat(request.matingDate)
            .isEqualTo(LocalDate.of(2026, 10, 21))
        assertThat(request.bornCount).isEqualTo(10)
        assertThat(request.aliveCount).isEqualTo(8)
        assertThat(request.femaleCount).isEqualTo(5)
        assertThat(request.maleCount).isEqualTo(3)
        assertThat(request.note).isEqualTo("Some note")
    }

    // -------------------------------------------------
    // PUT /api/matings/{id}
    // -------------------------------------------------

    @Test
    fun shouldUpdateMating() {

        val response = createResponse(
            id = 1L,
            femaleAnimalNumber = 2001,
            maleAnimalNumber = 3137
        )

        whenever(
            matingService.update(
                eq(1L),
                any<MatingRequest>()
            )
        ).thenReturn(response)

        val requestJson = createRequestJson(
            femaleAnimalNumber = 2001,
            maleAnimalNumber = 3137
        )

        mockMvc.perform(
            put("/api/matings/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.femaleAnimalNumber").value(2001))
            .andExpect(jsonPath("$.maleAnimalNumber").value(3137))
            .andExpect(jsonPath("$.matingDate").value("2026-10-21"))
            .andExpect(jsonPath("$.bornCount").value(10))

        val requestCaptor = argumentCaptor<MatingRequest>()

        verify(matingService).update(
            eq(1L),
            requestCaptor.capture()
        )

        assertThat(requestCaptor.firstValue.femaleAnimalNumber)
            .isEqualTo(2001)

        assertThat(requestCaptor.firstValue.maleAnimalNumber)
            .isEqualTo(3137)

        assertThat(requestCaptor.firstValue.matingDate)
            .isEqualTo(LocalDate.of(2026, 10, 21))

        assertThat(requestCaptor.firstValue.bornCount)
            .isEqualTo(10)
    }

    // -------------------------------------------------
    // DELETE /api/matings/{id}
    // -------------------------------------------------

    @Test
    fun shouldDeleteMating() {

        mockMvc.perform(
            delete("/api/matings/1")
        )
            .andExpect(status().isNoContent)
            .andExpect(content().string(""))

        verify(matingService).delete(1L)
    }

    @Test
    fun shouldReturn404WhenFindingMissingMating() {

        whenever(matingService.findById(999L))
            .thenThrow(
                NoSuchElementException(
                    "Error message NOT important"
                )
            )

        mockMvc.perform(
            get("/api/matings/999")
        )
            .andExpect(status().isNotFound)

        verify(matingService).findById(999L)
    }

    @Test
    fun shouldReturn404WhenUpdatingMissingMating() {

        whenever(
            matingService.update(
                eq(999L),
                any<MatingRequest>()
            )
        ).thenThrow(
            NoSuchElementException(
                "Error message NOT important"
            )
        )

        mockMvc.perform(
            put("/api/matings/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRequestJson(13, 19))
        )
            .andExpect(status().isNotFound)

        verify(matingService).update(
            eq(999L),
            any<MatingRequest>()
        )
    }

    @Test
    fun shouldReturn404WhenDeletingMissingMating() {

        doThrow(
            NoSuchElementException(
                "Error message NOT important"
            )
        ).whenever(matingService).delete(999L)

        mockMvc.perform(
            delete("/api/matings/999")
        )
            .andExpect(status().isNotFound)

        verify(matingService).delete(999L)
    }
    
    // -------------------------------------------------
    // Helpers
    // -------------------------------------------------

    private fun createResponse(
        id: Long,
        femaleAnimalNumber: Int,
        maleAnimalNumber: Int
    ): MatingResponse =
        MatingResponse(
            id = id,
            femaleAnimalNumber = femaleAnimalNumber,
            maleAnimalNumber = maleAnimalNumber,

            matingDate = LocalDate.of(2026, 10, 21),
            plugDate = null,
            pregnancyDate = null,
            birthDate = null,
            weaningDate = null,

            bornCount = 10,
            aliveCount = 8,
            femaleCount = 5,
            maleCount = 3,

            note = "Some note",
            endMatingDate = null
        )

    private fun createRequestJson(
        femaleAnimalNumber: Int,
        maleAnimalNumber: Int
    ): String =
        """
        {
            "femaleAnimalNumber": $femaleAnimalNumber,
            "maleAnimalNumber": $maleAnimalNumber,
    
            "matingDate": "2026-10-21",
        
            "bornCount": "10",
            "aliveCount": "8",
            "femaleCount": "5",
            "maleCount": "3",
    
            "note": "Some note"
        }
        """.trimIndent()
}