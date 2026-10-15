package cz.animalhouse.service

import cz.animalhouse.dto.MouseRequest
import cz.animalhouse.dto.MouseResponse
import cz.animalhouse.dto.ParentCandidateResponse
import cz.animalhouse.entity.Mouse
import cz.animalhouse.exception.DuplicateMouseAnimalNumberException
// import cz.animalhouse.repository.LabProcedureRepository
import cz.animalhouse.repository.MouseRepository
import cz.animalhouse.repository.StrainRepository
import cz.animalhouse.repository.TransgenicLineRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class MouseService(
    private val mouseRepository: MouseRepository,
    private val strainRepository: StrainRepository,
    private val transgenicLineRepository: TransgenicLineRepository,
    // private val labProcedureRepository: LabProcedureRepository
) {

    @Transactional(readOnly = true)
    fun findAll(pageable: Pageable): Page<MouseResponse> =
        mouseRepository.findAll(pageable)
            .map { it.toResponse() }

    @Transactional(readOnly = true)
    fun findById(id: Long): MouseResponse =
        findEntityById(id).toResponse()

    @Transactional(readOnly = true)
    fun findNextAnimalNumber(): Int =
        mouseRepository.findNextAnimalNumber()

    @Transactional
    fun create(request: MouseRequest): MouseResponse {

        validateParentsAnimalNumbers(
            animalNumber = request.animalNumber,
            motherAnimalNumber = request.motherAnimalNumber,
            fatherAnimalNumber = request.fatherAnimalNumber
        )

        if (mouseRepository.existsByAnimalNumber(request.animalNumber)) {
            throw DuplicateMouseAnimalNumberException(
                request.animalNumber
            )
        }

       validateDates(
            request.birthDate,
            request.deathDate
        )

        val strain = strainRepository.findById(request.strainId)
            .orElseThrow {
                NoSuchElementException(
                    "Strain with id=${request.strainId} not found"
                )
            }

        val transgenicLine = request.transgenicLineId?.let { id ->
            transgenicLineRepository.findById(id)
                .orElseThrow {
                    NoSuchElementException(
                        "Transgenic line with id=$id not found"
                    )
                }
        }

        // val labProcedure = request.labProcedureId?.let { id ->
        //     labProcedureRepository.findById(id)
        //         .orElseThrow {
        //             NoSuchElementException(
        //                 "Lab procedure with id=$id not found"
        //             )
        //         }
        // }

        val mother =
            findParentByAnimalNumber(request.motherAnimalNumber)

        val father =
            findParentByAnimalNumber(request.fatherAnimalNumber)

        validateParentsSex(
            mother = mother,
            father = father
        )

        val mouse = Mouse(
            animalNumber = request.animalNumber,
            sex = request.sex,
            strain = strain,
            transgenicLine = transgenicLine,
            // labProcedure = labProcedure,
            mother = mother,
            father = father,
            birthDate = request.birthDate,
            deathDate = request.deathDate,
            room = request.room,
            rack = request.rack,
            cage = request.cage,
            origin = request.origin,
            note = request.note
        )

        return mouseRepository.save(mouse)
            .toResponse()
    }

    @Transactional
    fun update(
        id: Long,
        request: MouseRequest
    ): MouseResponse {

        val mouse = findEntityById(id)

        validateParentsAnimalNumbers(
            animalNumber = request.animalNumber,
            motherAnimalNumber = request.motherAnimalNumber,
            fatherAnimalNumber = request.fatherAnimalNumber
        )

        if (mouseRepository.existsByAnimalNumberAndIdNot(
                request.animalNumber,
                id
            )
        ) {
            throw DuplicateMouseAnimalNumberException(
                request.animalNumber
            )
        }

        validateDates(
            request.birthDate,
            request.deathDate
        )

        val strain = strainRepository.findById(request.strainId)
            .orElseThrow {
                NoSuchElementException(
                    "Strain with id=${request.strainId} not found"
                )
            }

        val transgenicLine = request.transgenicLineId?.let { lineId ->
            transgenicLineRepository.findById(lineId)
                .orElseThrow {
                    NoSuchElementException(
                        "Transgenic line with id=$lineId not found"
                    )
                }
        }

        // val labProcedure = request.labProcedureId?.let { procedureId ->
        //     labProcedureRepository.findById(procedureId)
        //         .orElseThrow {
        //             NoSuchElementException(
        //                 "Lab procedure with id=$procedureId not found"
        //             )
        //         }
        // }

        val mother = request.motherId?.let {
            findEntityById(it)
        }

        val father = request.fatherId?.let {
            findEntityById(it)
        }

        validateParentsSex(
            mother = mother,
            father = father
        )

        mouse.animalNumber = request.animalNumber
        mouse.sex = request.sex
        mouse.strain = strain
        mouse.transgenicLine = transgenicLine
        // mouse.labProcedure = labProcedure
        mouse.mother = mother
        mouse.father = father
        mouse.birthDate = request.birthDate
        mouse.deathDate = request.deathDate
        mouse.room = request.room
        mouse.rack = request.rack
        mouse.cage = request.cage
        mouse.origin = request.origin
        mouse.note = request.note

        return mouse.toResponse()
    }

    @Transactional
    fun delete(id: Long) {
        val mouse = findEntityById(id)
        mouseRepository.delete(mouse)
    }

    private fun findEntityById(id: Long): Mouse =
        mouseRepository.findById(id)
        .orElseThrow {
            NoSuchElementException(
                "Mouse with id=$id not found"
            )
        }

    private fun findParentByAnimalNumber(
        animalNumber: Int?
    ): Mouse? {

        if (animalNumber == null) {
            return null
        }

        return mouseRepository
            .findByAnimalNumber(animalNumber)
            ?: throw IllegalArgumentException(
                "Mouse with animal number $animalNumber does not exist."
            )
    }

    @Transactional(readOnly = true)
    fun findParentCandidates(
        prefix: String,
        sex: String
    ): List<ParentCandidateResponse> =
        mouseRepository
            .findParentCandidates(prefix, sex)
            .map { mouse ->
                ParentCandidateResponse(
                    animalNumber = mouse.animalNumber
                )
            }
            
    private fun validateDates(
        birthDate: LocalDate?,
        deathDate: LocalDate?
    ) {
        if (
            birthDate != null &&
            deathDate != null &&
            deathDate.isBefore(birthDate)
        ) {
            throw IllegalArgumentException(
                "Death date cannot be before birth date"
            )
        }
    }

    private fun validateParentsAnimalNumbers(
        animalNumber: Int,
        motherAnimalNumber: Int?,
        fatherAnimalNumber: Int?
    ) {
        if (motherAnimalNumber != null && animalNumber == motherAnimalNumber) {
            throw IllegalArgumentException(
                "Mouse cannot be its own mother."
            )
        }

        if (fatherAnimalNumber != null && animalNumber == fatherAnimalNumber) {
            throw IllegalArgumentException(
                "Mouse cannot be its own father."
            )
        }

        if (motherAnimalNumber != null && motherAnimalNumber == fatherAnimalNumber) {
            throw IllegalArgumentException(
                "Mother and father must be different mice."
            )
        }
    }

        private fun validateParentsSex(
            mother: Mouse?,
            father: Mouse?
        ) {
        if (mother != null && mother.sex != Mouse.Sex.F) {
            throw IllegalArgumentException(
                "Mother must be female."
            )
        }

        if (father != null && father.sex != Mouse.Sex.M) {
            throw IllegalArgumentException(
                "Father must be male."
            )
        }
    }

    private fun Mouse.toResponse(): MouseResponse =
        MouseResponse(
            id = requireNotNull(id),
            animalNumber = animalNumber,
            sex = sex,

            strainId = requireNotNull(strain.id),
            strainCode = requireNotNull(strain.code),
            strainName = requireNotNull(strain.name),

            transgenicLineId = transgenicLine?.id,
            labProcedureId = labProcedure?.id,
            motherId = mother?.id,
            motherAnimalNumber = mother?.animalNumber,

            fatherId = father?.id,
            fatherAnimalNumber = father?.animalNumber,

            birthDate = birthDate,
            deathDate = deathDate,
            room = room,
            rack = rack,
            cage = cage,
            origin = origin,
            note = note
        )
}