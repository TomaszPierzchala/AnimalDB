package cz.animalhouse.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "lab_procedure_technician")
public class LabProcedureTechnician {

    @EmbeddedId
    private LabProcedureTechnicianId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("labProcedureId")
    @JoinColumn(name = "lab_procedure_id", nullable = false)
    private LabProcedure labProcedure;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("personId")
    @JoinColumn(name = "person_id", nullable = false)
    private Person technician;
    
    @Column(nullable = false, name = "assigned_date")
	private LocalDate assignedDate;
    
    private String note;

    protected LabProcedureTechnician() {
    }

    public LabProcedureTechnician(LabProcedure labProcedure, Person technician, LocalDate assignedDate, String note) {
		this.labProcedure = labProcedure;
		this.technician = technician;
		this.assignedDate = assignedDate;
		this.note = note;
	}

	public LabProcedureTechnicianId getId() {
        return id;
    }

    public LabProcedure getLabProcedure() {
        return labProcedure;
    }

    public Person getTechnician() {
        return technician;
    }
    
    public LocalDate getAssignedDate() {
		return assignedDate;
	}

	public String getNote() {
		return note;
	}

	
	public void setLabProcedure(LabProcedure labProcedure) {
		this.labProcedure = labProcedure;
	}

	public void setTechnician(Person technician) {
		this.technician = technician;
	}

	public void setAssignedDate(LocalDate assignedDate) {
		this.assignedDate = assignedDate;
	}

	public void setNote(String note) {
		this.note = note;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LabProcedureTechnician that = (LabProcedureTechnician) o;

        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "LabProcedureTechnician [EmbeddedId=" + id
                + ", assignedDate=" + assignedDate + "]";
    }
    
}