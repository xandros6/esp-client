package org.esp.domain.blueprint;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(QuantificationMethod.class)
public abstract class QuantificationMethod_ {

    public static volatile SingularAttribute<QuantificationMethod,String> label;

    public static volatile SingularAttribute<QuantificationMethod,Long> id;
}
