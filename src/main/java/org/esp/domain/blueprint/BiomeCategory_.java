package org.esp.domain.blueprint;

import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(BiomeCategory.class)
public abstract class BiomeCategory_ {

    public static volatile SingularAttribute<BiomeCategory,String> label;

    public static volatile SingularAttribute<BiomeCategory,Long> id;
}
