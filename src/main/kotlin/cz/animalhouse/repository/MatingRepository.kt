package cz.animalhouse.repository

import cz.animalhouse.entity.Mating
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository

interface MatingRepository : JpaRepository<Mating, Long> {

    @EntityGraph(attributePaths = ["female", "male"])
    override fun findAll(pageable: Pageable): Page<Mating>
}