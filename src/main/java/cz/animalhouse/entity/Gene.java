package cz.animalhouse.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA entity stores gene definitions and descriptions. 
 */
@Entity
@Table(name = "gene")
public class Gene {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String symbol;

    @Column
    private String description;

    protected Gene() {
        // Required by JPA
    }

	public Gene(String symbol, String description) {
		this.symbol = symbol;
		this.description = description;
	}

	public Long getId() {
		return id;
	}

	public String getSymbol() {
		return symbol;
	}

	public String getDescription() {
		return description;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
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

	    Gene pn = (Gene) o;

	    return id != null && id.equals(pn.id);
	}
	
	@Override
	public int hashCode() {
	    return getClass().hashCode();
	}

	@Override
	public String toString() {
		return "Gene [id=" + id + ", symbol=" + symbol + ", description=" + description + "]";
	}
	
	
}
