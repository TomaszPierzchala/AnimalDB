package cz.animalhouse.entity

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "mating")
class Mating(

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "female_id", nullable = false)
    var female: Mouse,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "male_id", nullable = false)
    var male: Mouse,

    @Column(name = "mating_date")
    var matingDate: LocalDate? = null,

    @Column(name = "plug_date")
    var plugDate: LocalDate? = null,

    @Column(name = "pregnancy_date")
    var pregnancyDate: LocalDate? = null,

    @Column(name = "birth_date")
    var birthDate: LocalDate? = null,

    @Column(name = "weaning_date")
    var weaningDate: LocalDate? = null,

    @Column(name = "end_mating_date")
    var endMatingDate: LocalDate? = null,

    @Column(name = "born_count")
    var bornCount: Int? = null,

    @Column(name = "alive_count")
    var aliveCount: Int? = null,

    @Column(name = "female_count")
    var femaleCount: Int? = null,

    @Column(name = "male_count")
    var maleCount: Int? = null,

    @Column(name = "note", columnDefinition = "TEXT")
    var note: String? = null

) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}
