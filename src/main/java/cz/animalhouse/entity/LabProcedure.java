package cz.animalhouse.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * JPA entity representing a laboratory procedure.
 */
@Entity
@Table(name = "lab_procedure")
public class LabProcedure {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(length = 200)
    private String name;

	@Column
	private String description;
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "director_id", nullable = false)
	private Person director;
	
	@Column(name = "start_date")
	private LocalDate startDate;

	@Column(name = "end_date")
	private LocalDate endDate;
	
	protected LabProcedure() {
	}

	public LabProcedure(String code, String name, String description, Person director, LocalDate startDate,
			LocalDate endDate) {
		this.code = code;
		this.name = name;
		this.description = description;
		this.director = director;
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public Long getId() {
		return id;
	}

	public String getCode() {
		return code;
	}


	public String getName() {
		return name;
	}


	public String getDescription() {
		return description;
	}


	public Person getDirector() {
		return director;
	}


	public LocalDate getStartDate() {
		return startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public void setCode(String code) {
		this.code = code;
	}


	public void setName(String name) {
		this.name = name;
	}


	public void setDescription(String description) {
		this.description = description;
	}


	public void setDirector(Person director) {
		this.director = director;
	}


	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		LabProcedure labProcedure = (LabProcedure) o;

		return id != null && id.equals(labProcedure.id);
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}

	@Override
	public String toString() {
		return "LabProcedure [id=" + id + ", code=" + code + ", name=" + name 
				+ ", startDate=" + startDate + ", endDate=" + endDate + "]";
	}

}
