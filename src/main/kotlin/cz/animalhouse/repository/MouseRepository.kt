package cz.animalhouse.repository

import cz.animalhouse.entity.Mouse
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface MouseRepository : JpaRepository<Mouse, Long> {

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
