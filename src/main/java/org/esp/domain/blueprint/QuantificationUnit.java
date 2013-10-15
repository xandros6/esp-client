package org.esp.domain.blueprint;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;

@Entity
@Table(schema = "blueprint", name = "quantification_unit")
public class QuantificationUnit {

    private Long id;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "seq")
    @SequenceGenerator(allocationSize = 1, name = "seq", sequenceName = "blueprint.unit_id_seq")
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

    private QuantificationUnitCategory quantificationUnitCategory;

    @ManyToOne
    @JoinColumn(name="quantification_unit_category_id")
    public QuantificationUnitCategory getQuantificationUnitCategory() {
        return quantificationUnitCategory;
    }

    public void setQuantificationUnitCategory(QuantificationUnitCategory quantificationUnitCategory) {
        this.quantificationUnitCategory = quantificationUnitCategory;
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
    
        if (obj instanceof QuantificationUnit) {
            QuantificationUnit otherObj = (QuantificationUnit) obj;
            if (otherObj.getId().equals(this.getId())) {
                return true;
            }
            return false;
        }
        return super.equals(obj);
    }
}
