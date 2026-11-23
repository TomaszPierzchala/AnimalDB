package cz.animalhouse.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "breeding")
data class BreedingProperties(
    val femaleMinAgeWeeks: Int,
    val maleMinAgeWeeks: Int
)