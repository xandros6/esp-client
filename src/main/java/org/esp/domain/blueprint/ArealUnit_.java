package org.esp.domain.blueprint;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(ArealUnit.class)
public abstract class ArealUnit_ {

    public static volatile SingularAttribute<ArealUnit,String> label;

    public static volatile SingularAttribute<ArealUnit,Long> id;
}
