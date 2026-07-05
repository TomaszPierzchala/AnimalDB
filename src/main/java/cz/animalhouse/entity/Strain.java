package cz.animalhouse.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA entity representing a mouse strain.
 */
@Entity
@Table(name = "strain")
public class Strain {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    protected Strain() {
        // Required by JPA
    }

	public Strain(String code, String name) {
		this.code = code;
		this.name = name;
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

	public void setCode(String code) {
		this.code = code;
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

	    Strain strain = (Strain) o;

	    return id != null && id.equals(strain.id);
	}
	
	@Override
	public int hashCode() {
	    return getClass().hashCode();
	}

	@Override
	public String toString() {
		return "Strain [id=" + id + ", code=" + code + ", name=" + name + "]";
	}
	
}
