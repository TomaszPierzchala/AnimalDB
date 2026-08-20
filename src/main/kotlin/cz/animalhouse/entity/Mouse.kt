package cz.animalhouse.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDate

/**
 * JPA entity representing a laboratory mouse.
 */
@Entity
@Table(name = "mouse")
class Mouse(

    @Column(
        name = "animal_number",
        nullable = false,
        unique = true
    )
    var animalNumber: Int,

    @Enumerated(EnumType.STRING)
    @Column(
        nullable = false,
        length = 1
    )
    var sex: Sex,

    @ManyToOne(
        fetch = FetchType.LAZY,
        optional = false
    )
    @JoinColumn(
        name = "strain_id",
        nullable = false
    )
    var strain: Strain,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transgenic_line_id")
    var transgenicLine: TransgenicLine? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_procedure_id")
    var labProcedure: LabProcedure? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mother_id")
    var mother: Mouse? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "father_id")
    var father: Mouse? = null,

    @Column(name = "birth_date")
    var birthDate: LocalDate? = null,

    @Column(name = "death_date")
    var deathDate: LocalDate? = null,

    @Column(length = 50)
    var room: String? = null,

    @Column(length = 50)
    var rack: String? = null,

    @Column(length = 50)
    var cage: String? = null,

    @Column(length = 100)
    var origin: String? = null,

    @Column
    var note: String? = null

) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        protected set

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is Mouse) {
            return false
        }

        return id != null && id == other.id
    }

    override fun hashCode(): Int =
        javaClass.hashCode()

    override fun toString(): String =
        "Mouse [(id=$id), animalNumber=$animalNumber, sex=$sex]"

    enum class Sex {
        M,
        F
    }
}