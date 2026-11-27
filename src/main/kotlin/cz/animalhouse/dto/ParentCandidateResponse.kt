package cz.animalhouse.dto

data class ParentCandidateResponse(
    val animalNumber: Int,
    val strainCode: String? = null,
    val strainName: String? = null,
    val name: String? = null
)