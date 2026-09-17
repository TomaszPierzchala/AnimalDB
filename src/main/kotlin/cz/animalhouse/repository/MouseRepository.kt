package cz.animalhouse.repository

import cz.animalhouse.entity.Mouse
import org.springframework.data.jpa.repository.JpaRepository

interface MouseRepository : JpaRepository<Mouse, Long> {

    fun existsByAnimalNumber(animalNumber: Int): Boolean

    fun existsByAnimalNumberAndIdNot(
        animalNumber: Int,
        id: Long
    ): Boolean
}
