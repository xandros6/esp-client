package org.esp.domain.blueprint;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(EcosystemService.class)
public abstract class EcosystemService_ {

    public static volatile SingularAttribute<EcosystemService,Long> id;

    public static volatile SingularAttribute<EcosystemService,String> label;

    public static volatile SingularAttribute<EcosystemService,String> description;

    public static volatile SingularAttribute<EcosystemService,EcosystemServiceCategory> ecosystemServiceCategory;

    public static volatile SingularAttribute<EcosystemService,ClassificationSystem> classificationSystem;

    public static volatile SingularAttribute<EcosystemService,String> iconUrl;
}
