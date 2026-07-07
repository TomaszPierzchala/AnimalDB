package cz.animalhouse.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class MouseGeneId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "mouse_id")
    private Long mouseId;

    @Column(name = "gene_id")
    private Long geneId;

    protected MouseGeneId() {
    }

    public MouseGeneId(Long mouseId, Long geneId) {
		this.mouseId = mouseId;
		this.geneId = geneId;
	}

	public Long getMouseId() {
		return mouseId;
	}

	public Long getGeneId() {
		return geneId;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MouseGeneId that)) return false;
        return Objects.equals(mouseId, that.mouseId)
                && Objects.equals(geneId, that.geneId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mouseId, geneId);
    }

	@Override
	public String toString() {
		return "(mouseId=" + mouseId + ", geneId=" + geneId + ")";
	}

}