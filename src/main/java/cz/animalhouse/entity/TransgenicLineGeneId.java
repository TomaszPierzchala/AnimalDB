package cz.animalhouse.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class TransgenicLineGeneId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "transgenic_line_id")
    private Long transgenicLineId;

    @Column(name = "gene_id")
    private Long geneId;

    protected TransgenicLineGeneId() {
    }

    public TransgenicLineGeneId(Long transgenicLineId, Long geneId) {
        this.transgenicLineId = transgenicLineId;
        this.geneId = geneId;
    }

    public Long getTransgenicLineId() {
		return transgenicLineId;
	}

	public Long getGeneId() {
		return geneId;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransgenicLineGeneId that)) return false;
        return Objects.equals(transgenicLineId, that.transgenicLineId)
                && Objects.equals(geneId, that.geneId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transgenicLineId, geneId);
    }

	@Override
	public String toString() {
		return "(transgenicLineId=" + transgenicLineId + ", geneId=" + geneId + ")";
	}

}