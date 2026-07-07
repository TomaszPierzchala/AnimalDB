package cz.animalhouse.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * JPA entity representing a laboratory mouse.
 */
@Entity
@Table(name = "mouse")
public class Mouse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "animal_number", nullable = false, unique = true)
    private Integer animalNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 1)
    private Sex sex;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "strain_id", nullable = false)
    private Strain strain;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transgenic_line_id")
    private TransgenicLine transgenicLine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_procedure_id")
    private LabProcedure labProcedure;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mother_id")
    private Mouse mother;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "father_id")
    private Mouse father;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "death_date")
    private LocalDate deathDate;

    @Column(length = 50)
    private String room;

    @Column(length = 50)
    private String rack;

    @Column(length = 50)
    private String cage;

    @Column(length = 100)
    private String origin;

    @Column
    private String note;

    protected Mouse() {
    }

    public Mouse(Integer animalNumber,
                 Sex sex,
                 Strain strain,
                 TransgenicLine transgenicLine,
                 LabProcedure labProcedure,
                 Mouse mother,
                 Mouse father,
                 LocalDate birthDate,
                 LocalDate deathDate,
                 String room,
                 String rack,
                 String cage,
                 String origin,
                 String note) {

        this.animalNumber = animalNumber;
        this.sex = sex;
        this.strain = strain;
        this.transgenicLine = transgenicLine;
        this.labProcedure = labProcedure;
        this.mother = mother;
        this.father = father;
        this.birthDate = birthDate;
        this.deathDate = deathDate;
        this.room = room;
        this.rack = rack;
        this.cage = cage;
        this.origin = origin;
        this.note = note;
    }

    public Long getId() {
		return id;
	}

	public Integer getAnimalNumber() {
		return animalNumber;
	}

	public Sex getSex() {
		return sex;
	}

	public Strain getStrain() {
		return strain;
	}

	public TransgenicLine getTransgenicLine() {
		return transgenicLine;
	}

	public LabProcedure getLabProcedure() {
		return labProcedure;
	}

	public Mouse getMother() {
		return mother;
	}

	public Mouse getFather() {
		return father;
	}

	public LocalDate getBirthDate() {
		return birthDate;
	}

	public LocalDate getDeathDate() {
		return deathDate;
	}

	public String getRoom() {
		return room;
	}

	public String getRack() {
		return rack;
	}

	public String getCage() {
		return cage;
	}

	public String getOrigin() {
		return origin;
	}

	public String getNote() {
		return note;
	}

	public void setAnimalNumber(Integer animalNumber) {
		this.animalNumber = animalNumber;
	}

	public void setSex(Sex sex) {
		this.sex = sex;
	}

	public void setStrain(Strain strain) {
		this.strain = strain;
	}

	public void setTransgenicLine(TransgenicLine transgenicLine) {
		this.transgenicLine = transgenicLine;
	}

	public void setLabProcedure(LabProcedure labProcedure) {
		this.labProcedure = labProcedure;
	}

	public void setMother(Mouse mother) {
		this.mother = mother;
	}

	public void setFather(Mouse father) {
		this.father = father;
	}

	public void setBirthDate(LocalDate birthDate) {
		this.birthDate = birthDate;
	}

	public void setDeathDate(LocalDate deathDate) {
		this.deathDate = deathDate;
	}

	public void setRoom(String room) {
		this.room = room;
	}

	public void setRack(String rack) {
		this.rack = rack;
	}

	public void setCage(String cage) {
		this.cage = cage;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public void setNote(String note) {
		this.note = note;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Mouse other)) {
            return false;
        }
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Mouse [(id=" + id
                + "), animalNumber=" + animalNumber
                + ", sex=" + sex
                + "]";
    }
    
    public enum Sex {
        M,
        F
    }
}