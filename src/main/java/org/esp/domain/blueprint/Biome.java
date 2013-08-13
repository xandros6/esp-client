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
import javax.persistence.ManyToMany;
import javax.persistence.JoinTable;
import javax.persistence.CascadeType;
import java.util.Set;

@Entity
@Table(schema = "blueprint", name = "biome")
public class Biome {

    private Long id;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "seq")
    @SequenceGenerator(allocationSize = 1, name = "seq", sequenceName = "blueprint.biome_id_seq")
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

    private BiomeCategory biomeCategory;

    @ManyToOne
    @JoinColumn(name="biome_category_id")
    public BiomeCategory getBiomeCategory() {
        return biomeCategory;
    }

    public void setBiomeCategory(BiomeCategory biomeCategory) {
        this.biomeCategory = biomeCategory;
    }

    private Set<EcosystemServiceIndicator> ecosystemServiceIndicators;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "blueprint.ecosystem_service_indicator_biome", joinColumns = @JoinColumn(name = "biome_id"), inverseJoinColumns = @JoinColumn(name = "ecosystem_service_indicator_id"))
    public Set<EcosystemServiceIndicator> getEcosystemServiceIndicators() {
        return ecosystemServiceIndicators;
    }

    public void setEcosystemServiceIndicators(Set<EcosystemServiceIndicator> ecosystemServiceIndicators) {
        this.ecosystemServiceIndicators = ecosystemServiceIndicators;
    }

    @Override
    public String toString() {
        return label;
    }
}
