package org.esp.domain.blueprint;

import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(EcosystemServiceIndicatorBiome.class)
public abstract class EcosystemServiceIndicatorBiome_ {

    public static volatile SingularAttribute<EcosystemServiceIndicatorBiome,EcosystemServiceIndicator> ecosystemServiceIndicator;

    public static volatile SingularAttribute<EcosystemServiceIndicatorBiome,Biome> biome;

    public static volatile SingularAttribute<EcosystemServiceIndicatorBiome,Long> id;
}
