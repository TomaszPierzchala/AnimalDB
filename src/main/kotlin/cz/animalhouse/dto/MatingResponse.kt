package cz.animalhouse.dto

import java.time.LocalDate

data class MatingResponse(
    val id: Long,
    val femaleAnimalNumber: Int,
    val maleAnimalNumber: Int,

    val matingDate: LocalDate?,
    val plugDate: LocalDate?,
    val pregnancyDate: LocalDate?,
    val birthDate: LocalDate?,
    val weaningDate: LocalDate?,

    val bornCount: Int?,
    val aliveCount: Int?,
    val femaleCount: Int?,
    val maleCount: Int?,

    val note: String?,
    val endMatingDate: LocalDate?
)
