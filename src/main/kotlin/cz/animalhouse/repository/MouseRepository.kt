package cz.animalhouse.repository

import cz.animalhouse.entity.Mouse
import org.springframework.data.jpa.repository.JpaRepository

interface MouseRepository : JpaRepository<Mouse, Long>
