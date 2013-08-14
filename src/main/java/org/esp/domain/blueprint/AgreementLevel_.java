package org.esp.domain.blueprint;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(AgreementLevel.class)
public abstract class AgreementLevel_ {

    public static volatile SingularAttribute<AgreementLevel,String> label;

    public static volatile SingularAttribute<AgreementLevel,Long> id;
}
