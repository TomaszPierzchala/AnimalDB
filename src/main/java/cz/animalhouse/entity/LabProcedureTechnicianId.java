package cz.animalhouse.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class LabProcedureTechnicianId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "lab_procedure_id")
    private Long labProcedureId;

    @Column(name = "person_id")
    private Long personId;

    protected LabProcedureTechnicianId() {
    }

    public LabProcedureTechnicianId(Long labProcedureId, Long personId) {
        this.labProcedureId = labProcedureId;
        this.personId = personId;
    }

    public Long getLabProcedureId() {
        return labProcedureId;
    }

    public Long getPersonId() {
        return personId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LabProcedureTechnicianId that)) return false;
        return Objects.equals(labProcedureId, that.labProcedureId)
                && Objects.equals(personId, that.personId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(labProcedureId, personId);
    }

	@Override
	public String toString() {
		return "(labProcedureId=" + labProcedureId + ", personId=" + personId + ")";
	}
    
    
    
}