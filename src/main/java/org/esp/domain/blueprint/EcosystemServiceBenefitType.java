package org.esp.domain.blueprint;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(schema = "blueprint", name = "ecosystem_service_benefit_type")
public class EcosystemServiceBenefitType {

    private Long id;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "seq")
    @SequenceGenerator(allocationSize = 1, name = "seq", sequenceName = "blueprint.ecosystem_service_benefit_type_id_seq")
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    private String label;

    @NotNull
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
    public boolean equals(Object obj) {
    
        if (obj instanceof EcosystemServiceBenefitType) {
            EcosystemServiceBenefitType comparee = (EcosystemServiceBenefitType) obj;
            if (comparee.getId().equals(getId())) {
                return true;
            }
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        if (id != null) {
            return id.intValue();
        }
        return super.hashCode();
    }
}
