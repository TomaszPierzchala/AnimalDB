package cz.animalhouse.controller

import cz.animalhouse.dto.MatingResponse
import cz.animalhouse.service.MatingService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
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
}