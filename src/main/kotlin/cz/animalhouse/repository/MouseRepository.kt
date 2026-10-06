package cz.animalhouse.repository

import cz.animalhouse.entity.Mouse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

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
}
