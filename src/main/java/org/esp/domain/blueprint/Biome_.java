package org.esp.domain.blueprint;

import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(Biome.class)
public abstract class Biome_ {

    public static volatile SingularAttribute<Biome,Long> id;

    public static volatile SingularAttribute<Biome,String> label;

    public static volatile SingularAttribute<Biome,BiomeCategory> biomeCategory;

    public static volatile SetAttribute<Biome,EcosystemServiceIndicator> ecosystemServiceIndicators;
}
