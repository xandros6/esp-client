package org.esp.domain.blueprint;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;

@Entity
@Table(schema = "blueprint", name = "ecosystem_service_indicator_biome")
public class EcosystemServiceIndicatorBiome {

    private Long id;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "seq")
    @SequenceGenerator(allocationSize = 1, name = "seq", sequenceName = "blueprint.ecosystem_service_indicator_biome_id_seq")
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    private EcosystemServiceIndicator ecosystemServiceIndicator;

    @ManyToOne
    @JoinColumn(name="ecosystem_service_indicator_id")
    public EcosystemServiceIndicator getEcosystemServiceIndicator() {
        return ecosystemServiceIndicator;
    }

    public void setEcosystemServiceIndicator(EcosystemServiceIndicator ecosystemServiceIndicator) {
        this.ecosystemServiceIndicator = ecosystemServiceIndicator;
    }

    private Biome biome;

    @ManyToOne
    @JoinColumn(name="biome_id")
    public Biome getBiome() {
        return biome;
    }

    public void setBiome(Biome biome) {
        this.biome = biome;
    }
    
    @Override
    public boolean equals(Object obj) {
    
        if (obj instanceof EcosystemServiceIndicatorBiome) {
            EcosystemServiceIndicatorBiome comparee = (EcosystemServiceIndicatorBiome) obj;
            if (comparee.getId().equals(getId())) {
                return true;
            }
            return false;
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
