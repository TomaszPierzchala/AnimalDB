package cz.animalhouse.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "transgenic_line_gene")
public class TransgenicLineGene {

    @EmbeddedId
    private TransgenicLineGeneId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("transgenicLineId")
    @JoinColumn(name = "transgenic_line_id", nullable = false)
    private TransgenicLine transgenicLine;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("geneId")
    @JoinColumn(name = "gene_id", nullable = false)
    private Gene gene;
    
    protected TransgenicLineGene() {
    }

	public TransgenicLineGene(TransgenicLine transgenicLine, Gene gene) {
		this.transgenicLine = transgenicLine;
		this.gene = gene;
	}

	public TransgenicLineGeneId getId() {
		return id;
	}

	public TransgenicLine getTransgenicLine() {
		return transgenicLine;
	}

	public Gene getGene() {
		return gene;
	}

	public void setTransgenicLine(TransgenicLine transgenicLine) {
		this.transgenicLine = transgenicLine;
	}

	public void setGene(Gene gene) {
		this.gene = gene;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TransgenicLineGene that = (TransgenicLineGene) o;

        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "TransgenicLineGene [EmbeddedId=" + id + "]";
    }
    
}