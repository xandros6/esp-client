package org.esp.domain.blueprint;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(SpatialDataType.class)
public abstract class SpatialDataType_ {

    public static volatile SingularAttribute<SpatialDataType,String> label;

    public static volatile SingularAttribute<SpatialDataType,Long> id;
}
