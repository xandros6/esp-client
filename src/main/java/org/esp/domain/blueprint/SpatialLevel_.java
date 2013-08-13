package org.esp.domain.blueprint;

import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(SpatialLevel.class)
public abstract class SpatialLevel_ {

    public static volatile SingularAttribute<SpatialLevel,String> label;

    public static volatile SingularAttribute<SpatialLevel,Long> id;
}
