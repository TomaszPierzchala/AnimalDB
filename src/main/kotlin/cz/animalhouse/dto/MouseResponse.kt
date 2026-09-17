package cz.animalhouse.dto

import cz.animalhouse.entity.Mouse.Sex
import java.time.LocalDate

data class MouseResponse(
    val id: Long,
    val animalNumber: Int,
    val sex: Sex,
    val strainId: Long,
    val transgenicLineId: Long?,
    val labProcedureId: Long?,
    val motherId: Long?,
    val fatherId: Long?,
    val birthDate: LocalDate?,
    val deathDate: LocalDate?,
    val room: String?,
    val rack: String?,
    val cage: String?,
    val origin: String?,
    val note: String?
)