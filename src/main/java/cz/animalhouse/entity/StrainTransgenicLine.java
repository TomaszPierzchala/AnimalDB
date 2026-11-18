package cz.animalhouse.entity;

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
 * JPA entity representing a transgenic mouse line.
 */
@Entity
@Table(name = "strain_transgenic_line")
public class StrainTransgenicLine {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "strain_id", nullable = false)
	private Strain strain;

	@Column(nullable = true, unique = true, length = 100)
	private String name;

	protected StrainTransgenicLine() {
	}

	public StrainTransgenicLine(Strain strain, String name) {
		this.strain = strain;
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public Strain getStrain() {
		return strain;
	}

	public String getName() {
		return name;
	}

	public void setStrain(Strain strain) {
		this.strain = strain;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		StrainTransgenicLine strainTransgenicLine = (StrainTransgenicLine) o;

		return id != null && id.equals(strainTransgenicLine.id);
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}

	@Override
	public String toString() {
		return "StrainTransgenicLine [id=" + id + "strain=" + strain + ", name=" + name + "]";
	}

}
