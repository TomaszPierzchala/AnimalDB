package cz.animalhouse.controller

import cz.animalhouse.dto.MouseRequest
import cz.animalhouse.dto.MouseResponse
import cz.animalhouse.service.MouseService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@CrossOrigin(origins = ["\${app.cors.allowed-origin}"])
@RestController
@RequestMapping("/api/mice")
class MouseController(
    private val mouseService: MouseService
) {

    @GetMapping
    fun getAllMice(
        pageable: Pageable
    ): Page<MouseResponse> =
        mouseService.findAll(pageable)

    @GetMapping("/{id}")
    fun getMouseById(
        @PathVariable id: Long
    ): MouseResponse =
        mouseService.findById(id)

    @GetMapping("/next-animal-number")
    fun getNextAnimalNumber(): Int =
        mouseService.findNextAnimalNumber()

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createMouse(
        @Valid @RequestBody request: MouseRequest
    ): MouseResponse =
        mouseService.create(request)

    @PutMapping("/{id}")
    fun updateMouse(
        @PathVariable id: Long,
        @Valid @RequestBody request: MouseRequest
    ): MouseResponse =
        mouseService.update(id, request)

    @DeleteMapping("/{id}")
    fun deleteMouse(
        @PathVariable id: Long
    ): ResponseEntity<Void> {
        mouseService.delete(id)
        return ResponseEntity.noContent().build()
    }
}