package cz.animalhouse.service

import cz.animalhouse.dto.MouseRequest
import cz.animalhouse.dto.MouseResponse
import cz.animalhouse.entity.Mouse
import cz.animalhouse.exception.DuplicateMouseAnimalNumberException
// import cz.animalhouse.repository.LabProcedureRepository
import cz.animalhouse.repository.MouseRepository
import cz.animalhouse.repository.StrainRepository
import cz.animalhouse.repository.TransgenicLineRepository
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
    fun findAll(): List<MouseResponse> =
        mouseRepository.findAll()
            .map { it.toResponse() }

    @Transactional(readOnly = true)
    fun findById(id: Long): MouseResponse =
        findEntityById(id).toResponse()

    @Transactional
    fun create(request: MouseRequest): MouseResponse {
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

        val mother = request.motherId?.let {
            findEntityById(it)
        }

        val father = request.fatherId?.let {
            findEntityById(it)
        }

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

    private fun Mouse.toResponse(): MouseResponse =
        MouseResponse(
            id = requireNotNull(id),
            animalNumber = animalNumber,
            sex = sex,
            strainId = requireNotNull(strain.id),
            transgenicLineId = transgenicLine?.id,
            labProcedureId = labProcedure?.id,
            motherId = mother?.id,
            fatherId = father?.id,
            birthDate = birthDate,
            deathDate = deathDate,
            room = room,
            rack = rack,
            cage = cage,
            origin = origin,
            note = note
        )
}