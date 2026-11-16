
package cz.animalhouse.service

import cz.animalhouse.dto.MatingRequest
import cz.animalhouse.repository.MatingRepository
import cz.animalhouse.repository.MouseRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.jdbc.Sql
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import java.time.LocalDate

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Transactional
@Sql("/sql/mating_test_mice.sql")
class MatingServiceIntegrationTest {

    companion object {
        @Container
        @ServiceConnection
        @JvmStatic
        val postgres = PostgreSQLContainer("postgres:17")
    }

    @Autowired
    lateinit var matingService: MatingService

    @Autowired
    lateinit var matingRepository: MatingRepository

    @Autowired
    lateinit var mouseRepository: MouseRepository

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    @Test
    fun `create sets endMatingDate from weaningDate via database trigger`() {
        val weaningDate = LocalDate.of(2026, 11, 1)

        val request = MatingRequest(
            femaleAnimalNumber = 1001,
            maleAnimalNumber = 1002,
            matingDate = LocalDate.of(2026, 10, 1),
            plugDate = null,
            pregnancyDate = null,
            birthDate = null,
            weaningDate = weaningDate,
            bornCount = null,
            aliveCount = null,
            femaleCount = null,
            maleCount = null,
            note = null
        )

        // Act: real service, repository, EntityManager and PostgreSQL
        val response = matingService.create(request)

        // Assert: response reflects the database trigger
        assertNotNull(response.id)
        assertEquals(weaningDate, response.weaningDate)
        assertEquals(weaningDate, response.endMatingDate)

        // Assert: the value is actually stored in PostgreSQL
        val storedEndDate = jdbcTemplate.queryForObject(
            "SELECT end_mating_date FROM mating WHERE id = ?",
            LocalDate::class.java,
            response.id
        )

        assertEquals(weaningDate, storedEndDate)

        // or by JPA
        val loaded = matingRepository.findById(response.id)
        assertNotNull(loaded)
        assertEquals(weaningDate, loaded.get().endMatingDate)
    }
}
