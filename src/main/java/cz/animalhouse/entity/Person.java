package cz.animalhouse.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA entity representing a laboratory staff member.
 */
@Entity
@Table(name = "person")
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column
    private String description;

    protected Person() {
        // Required by JPA
    }

	public Person(String name, String description) {
		this.name = name;
		this.description = description;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public void setName(String name) {
		this.name = name;
	}

	
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public boolean equals(Object o) {
	    if (this == o) {
	        return true;
	    }

	    if (o == null || getClass() != o.getClass()) {
	        return false;
	    }

	    Person pn = (Person) o;

	    return id != null && id.equals(pn.id);
	}
	
	@Override
	public int hashCode() {
	    return getClass().hashCode();
	}

	@Override
	public String toString() {
		return "Person [id=" + id + ", name=" + name + ", description=" + description + "]";
	}
	
	
}
