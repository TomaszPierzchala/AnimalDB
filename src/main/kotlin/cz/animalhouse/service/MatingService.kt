package cz.animalhouse.service

import cz.animalhouse.dto.MatingRequest
import cz.animalhouse.dto.MatingResponse
import cz.animalhouse.entity.Mating
import cz.animalhouse.entity.Mouse
import cz.animalhouse.repository.MatingRepository
import jakarta.persistence.EntityManager
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.Long

@Service
@Transactional(readOnly = true)
class MatingService(
    private val matingRepository: MatingRepository,
    private val mouseService: MouseService,
    private val entityManager: EntityManager
) {

    fun findAll(pageable: Pageable): Page<MatingResponse> =
        matingRepository.findAll(pageable)
            .map { it.toResponse() }

    fun findById(id: Long): MatingResponse =
        findEntityById(id).toResponse()

    @Transactional
    fun create(request: MatingRequest): MatingResponse {
        validate(request)

        val female = findMouseByAnimalNumber(request.femaleAnimalNumber)
        val male = findMouseByAnimalNumber(request.maleAnimalNumber)

        validateSex(female = female, male = male)
        val mating = Mating(
            female = female,
            male = male
        )

        applyRequest(mating, request)

        val saved = matingRepository.saveAndFlush(mating)
        entityManager.refresh(saved)

        return saved.toResponse()
    }

    @Transactional
    fun update(id: Long, request: MatingRequest): MatingResponse {
        val mating = findEntityById(id)

        validate(request)

        val female = findMouseByAnimalNumber(request.femaleAnimalNumber)
        val male = findMouseByAnimalNumber(request.maleAnimalNumber)

        validateSex(female, male)

        mating.female = female
        mating.male = male

        applyRequest(mating, request)

        val saved = matingRepository.saveAndFlush(mating)
        entityManager.refresh(saved)

        return saved.toResponse()
    }

    @Transactional
    fun delete(id: Long) {
        val mating = findEntityById(id)
        matingRepository.delete(mating)
    }

    private fun findMouseByAnimalNumber(animalNumber: Int?): Mouse {
            requireNotNull(animalNumber) {
                "Mouse animal number must not be null"
            }

            return mouseService.findMouseByAnimalNumber(animalNumber)!!
    }

    private fun findEntityById(id: Long): Mating =
        matingRepository.findById(id)
            .orElseThrow {
                NoSuchElementException(
                    "Mating with id=$id not found"
                )
            }

    private fun applyRequest(
        mating: Mating,
        request: MatingRequest
    ) {
        mating.matingDate = request.matingDate
        mating.plugDate = request.plugDate
        mating.pregnancyDate = request.pregnancyDate
        mating.birthDate = request.birthDate
        mating.weaningDate = request.weaningDate

        mating.bornCount = request.bornCount
        mating.aliveCount = request.aliveCount
        mating.femaleCount = request.femaleCount
        mating.maleCount = request.maleCount

        mating.note = request.note
    }

    private fun validate(request: MatingRequest) {
        require(request.femaleAnimalNumber != request.maleAnimalNumber) {
            "Female and male must be different mice"
        }

        validateDates(request)
        validateCounts(request)
    }

    private fun validateSex(female: Mouse, male: Mouse) {
        require(female.sex == Mouse.Sex.F) { "Female mouse must have sex F" }
        require(male.sex == Mouse.Sex.M) { "Male mouse must have sex M" }
    }

    private fun validateDates(request: MatingRequest) {
        val dates = listOf(
            "matingDate" to request.matingDate,
            "plugDate" to request.plugDate,
            "pregnancyDate" to request.pregnancyDate,
            "birthDate" to request.birthDate,
            "weaningDate" to request.weaningDate
        ).filter { it.second != null }

        dates.zipWithNext().forEach { (previous, next) ->
            val (previousName, previousDate) = previous
            val (nextName, nextDate) = next

            require(previousDate!!.isBefore(nextDate!!)
            ) {
                "$previousName must be before $nextName"
            }
        }
    }

    private fun validateCounts(request: MatingRequest) {
        val counts = listOf(
            "bornCount" to request.bornCount,
            "aliveCount" to request.aliveCount,
            "femaleCount" to request.femaleCount,
            "maleCount" to request.maleCount
        )

        counts.forEach { (name, count) ->
            require(count == null || count >= 0 ){
                "$name cannot be negative"
            }
            require(count == null || count <= 20 ){
                "Biology limits $name to values no more then 20"
            }
        }

        val born = request.bornCount
        val alive = request.aliveCount
        val female = request.femaleCount
        val male = request.maleCount

        require(born == null || alive == null || alive <= born) {
            "aliveCount must not exceed bornCount"
        }

        require(alive == null || female == null || female <= alive) {
            "femaleCount must not exceed aliveCount"
        }

        require(alive == null || male == null || male <= alive) {
            "maleCount must not exceed aliveCount"
        }

        require(
            alive == null || female == null || male == null ||
                    female + male <= alive
        ) {
            "femaleCount + maleCount must not exceed aliveCount"
        }
    }

    private fun Mating.toResponse(): MatingResponse =
        MatingResponse(
            id = requireNotNull(id),
            femaleAnimalNumber = female.animalNumber,
            maleAnimalNumber = male.animalNumber,

            matingDate = matingDate,
            plugDate = plugDate,
            pregnancyDate = pregnancyDate,
            birthDate = birthDate,
            weaningDate = weaningDate,

            bornCount = bornCount,
            aliveCount = aliveCount,
            femaleCount = femaleCount,
            maleCount = maleCount,

            note = note,
            endMatingDate = endMatingDate
    )
}
