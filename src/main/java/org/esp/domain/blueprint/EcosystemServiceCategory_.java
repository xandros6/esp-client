package org.esp.domain.blueprint;

import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(EcosystemServiceCategory.class)
public abstract class EcosystemServiceCategory_ {

    public static volatile SingularAttribute<EcosystemServiceCategory,Long> id;

    public static volatile SingularAttribute<EcosystemServiceCategory,String> label;
}
