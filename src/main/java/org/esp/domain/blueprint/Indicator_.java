package org.esp.domain.blueprint;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(Indicator.class)
public abstract class Indicator_ {

    public static volatile SingularAttribute<Indicator,String> label;

    public static volatile SingularAttribute<Indicator,Long> id;
}
