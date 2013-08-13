package org.esp.domain.blueprint;

import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(DataSource.class)
public abstract class DataSource_ {

    public static volatile SingularAttribute<DataSource,String> label;

    public static volatile SingularAttribute<DataSource,String> url;

    public static volatile SingularAttribute<DataSource,Long> id;

    public static volatile SetAttribute<DataSource,EcosystemServiceIndicator> ecosystemServiceIndicators;
}
