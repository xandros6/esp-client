package org.esp.domain.blueprint;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(QuantificationUnit.class)
public abstract class QuantificationUnit_ {

    public static volatile SingularAttribute<QuantificationUnit,String> label;

    public static volatile SingularAttribute<QuantificationUnit,Long> id;

    public static volatile SingularAttribute<QuantificationUnit,QuantificationUnitCategory> quantificationUnitCategory;
}
