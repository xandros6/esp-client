package org.esp.domain.blueprint;

import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(IndicatorDataSource.class)
public abstract class IndicatorDataSource_ {

    public static volatile SingularAttribute<IndicatorDataSource,EcosystemServiceIndicator> ecosystemServiceIndicator;

    public static volatile SingularAttribute<IndicatorDataSource,DataSource> dataSource;

    public static volatile SingularAttribute<IndicatorDataSource,Long> id;
}
