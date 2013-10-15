package org.esp.domain.blueprint;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(schema = "blueprint", name = "study_objective_met")
public class StudyObjectiveMet {

    private Long id;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "seq")
    @SequenceGenerator(allocationSize = 1, name = "seq", sequenceName = "blueprint.agreement_level_id_seq")
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    private String label;

    @Column
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
    
    @Override
    public int hashCode() {
        if (id != null) {
            return id.intValue();
        }
        return super.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
    
        if (obj instanceof StudyObjectiveMet) {
            StudyObjectiveMet comparee = (StudyObjectiveMet) obj;
            if (comparee.getId().equals(getId())) {
                return true;
            }
            return false;
        }
        return super.equals(obj);
    }
    
}
