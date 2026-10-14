package cz.animalhouse.dto

import cz.animalhouse.entity.Mouse.Sex
import java.time.LocalDate

data class MouseResponse(
    val id: Long,
    val animalNumber: Int,
    val sex: Sex,

    val strainId: Long,
    val strainCode: String,
    val strainName: String,

    val transgenicLineId: Long?,
    val labProcedureId: Long?,

    val motherId: Long?,
    val motherAnimalNumber: Int?,

    val fatherId: Long?,
    val fatherAnimalNumber: Int?,

    val birthDate: LocalDate?,
    val deathDate: LocalDate?,
    val room: String?,
    val rack: String?,
    val cage: String?,
    val origin: String?,
    val note: String?
)