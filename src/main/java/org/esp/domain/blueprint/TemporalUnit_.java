package org.esp.domain.blueprint;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(TemporalUnit.class)
public abstract class TemporalUnit_ {

    public static volatile SingularAttribute<TemporalUnit,String> label;

    public static volatile SingularAttribute<TemporalUnit,Long> id;
}
