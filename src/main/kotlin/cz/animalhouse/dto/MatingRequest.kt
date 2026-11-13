package cz.animalhouse.dto

import java.time.LocalDate

data class MatingRequest(
    val femaleAnimalNumber: Int?,
    val maleAnimalNumber: Int?,

    val matingDate: LocalDate? = null,
    val plugDate: LocalDate? = null,
    val pregnancyDate: LocalDate? = null,
    val birthDate: LocalDate? = null,
    val weaningDate: LocalDate? = null,

    val bornCount: Int? = null,
    val aliveCount: Int? = null,
    val femaleCount: Int? = null,
    val maleCount: Int? = null,

    val note: String? = null,
    val endMatingDate: LocalDate? = null
)
