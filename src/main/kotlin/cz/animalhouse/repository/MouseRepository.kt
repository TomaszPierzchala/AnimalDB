package cz.animalhouse.repository

import cz.animalhouse.entity.Mouse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate

interface MouseRepository : JpaRepository<Mouse, Long> {

    @EntityGraph(attributePaths = ["strain"])
    override fun findAll(pageable: Pageable): Page<Mouse>

    fun existsByAnimalNumber(animalNumber: Int): Boolean

    fun existsByAnimalNumberAndIdNot(
        animalNumber: Int,
        id: Long
    ): Boolean

    @Query(
        """
        select coalesce(max(m.animalNumber), 0) + 1
        from Mouse m
        """
    )
    fun findNextAnimalNumber(): Int

    fun findByAnimalNumber(animalNumber: Int): Mouse?

    @Query(
        value = """
        SELECT *
        FROM mouse
        WHERE CAST(animal_number AS TEXT) LIKE CONCAT('%', :infix, '%')
          AND sex = :sex
          AND death_date IS NULL
          AND (
                birth_date IS NULL
                OR (
                    sex = 'F'
                    AND birth_date < CAST(:currentDate AS DATE)
                        - (:femaleMinAgeWeeks * 7)
                )
                OR (
                    sex = 'M'
                    AND birth_date < CAST(:currentDate AS DATE)
                        - (:maleMinAgeWeeks * 7)
                )
              )
        ORDER BY
            birth_date DESC NULLS FIRST
        LIMIT 10
    """,
        nativeQuery = true
    )
    fun findParentCandidates(
        @Param("infix") infix: String,
        @Param("sex") sex: String,
        @Param("currentDate") currentDate: LocalDate,
        @Param("femaleMinAgeWeeks") femaleMinAgeWeeks: Int,
        @Param("maleMinAgeWeeks") maleMinAgeWeeks: Int
    ): List<Mouse>

    fun existsByMotherId(id: Long): Boolean

    fun existsByFatherId(id: Long): Boolean
}
