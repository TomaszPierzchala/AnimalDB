package cz.animalhouse.controller

import cz.animalhouse.dto.MatingResponse
import cz.animalhouse.dto.MatingRequest
import cz.animalhouse.service.MatingService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@CrossOrigin(origins = ["\${app.cors.allowed-origin}"])
@RestController
@RequestMapping("/api/matings")
class MatingController(
    private val matingService: MatingService
) {

    @GetMapping
    fun findAll(pageable: Pageable
    ): Page<MatingResponse> =
        matingService.findAll(pageable)

    @GetMapping("/{id}")
    fun getMatingById(
        @PathVariable id: Long
    ): MatingResponse =
        matingService.findById(id)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createMating(
        @Valid @RequestBody request: MatingRequest
    ): MatingResponse =
        matingService.create(request)

    @PutMapping("/{id}")
    fun updateMating(
        @PathVariable id: Long,
        @Valid @RequestBody request: MatingRequest
    ): MatingResponse =
        matingService.update(id, request)

    @DeleteMapping("/{id}")
    fun deleteMating(
        @PathVariable id: Long
    ): ResponseEntity<Void> {
        matingService.delete(id)
        return ResponseEntity.noContent().build()
    }
}