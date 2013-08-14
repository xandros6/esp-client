package org.esp.domain.blueprint;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(QuantificationUnitCategory.class)
public abstract class QuantificationUnitCategory_ {

    public static volatile SingularAttribute<QuantificationUnitCategory,String> label;

    public static volatile SingularAttribute<QuantificationUnitCategory,Long> id;
}
