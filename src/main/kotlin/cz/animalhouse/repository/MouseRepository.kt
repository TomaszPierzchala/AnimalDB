package cz.animalhouse.repository

import cz.animalhouse.entity.Mouse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

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
        WHERE CAST(animal_number AS TEXT) LIKE CONCAT(:prefix, '%')
          AND sex = :sex
        ORDER BY animal_number
        LIMIT 10
    """,
        nativeQuery = true
    )
    fun findParentCandidates(
        @Param("prefix") prefix: String,
        @Param("sex") sex: String
    ): List<Mouse>
}
