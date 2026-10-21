package cz.animalhouse.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@CrossOrigin(origins = ["\${app.cors.allowed-origin}"])
@RestController
@RequestMapping("/api/matings")
class MatingController(
//    private val matingService: MatingService
) {

    @GetMapping
    fun findAll(): ResponseEntity<Void> =
        ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build()
}