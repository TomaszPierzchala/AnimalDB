package cz.animalhouse.dto

import cz.animalhouse.entity.Mouse.Sex
import java.time.LocalDate

data class MouseRequest(
    val animalNumber: Int,
    val sex: Sex,
    val strainId: Long,
    val transgenicLineId: Long? = null,
    val labProcedureId: Long? = null,
    val motherId: Long? = null,
    val fatherId: Long? = null,
    val birthDate: LocalDate? = null,
    val deathDate: LocalDate? = null,
    val room: String? = null,
    val rack: String? = null,
    val cage: String? = null,
    val origin: String? = null,
    val note: String? = null
)