package org.esp.domain.blueprint;

import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

import org.jrc.persist.adminunits.Grouping;

@StaticMetamodel(EcosystemServiceIndicator.class)
public abstract class EcosystemServiceIndicator_ {

    public static volatile SingularAttribute<EcosystemServiceIndicator,EcosystemService> ecosystemService;

    public static volatile SingularAttribute<EcosystemServiceIndicator,Study> study;

    public static volatile SingularAttribute<EcosystemServiceIndicator,StudyObjectiveMet> studyObjectiveMet;

    public static volatile SingularAttribute<EcosystemServiceIndicator,SpatialLevel> spatialLevel;

    public static volatile SingularAttribute<EcosystemServiceIndicator,QuantificationUnit> quantificationUnit;

    public static volatile SingularAttribute<EcosystemServiceIndicator,ArealUnit> arealUnit;

    public static volatile SingularAttribute<EcosystemServiceIndicator,Indicator> indicator;

    public static volatile SingularAttribute<EcosystemServiceIndicator,TemporalUnit> temporalUnit;

    public static volatile SingularAttribute<EcosystemServiceIndicator,QuantificationMethod> quantificationMethod;

    public static volatile SingularAttribute<EcosystemServiceIndicator,SpatialDataType> spatialDataType;

    public static volatile SingularAttribute<EcosystemServiceIndicator,EcosystemServiceAccountingType> ecosystemServiceAccountingType;

    public static volatile SingularAttribute<EcosystemServiceIndicator,EcosystemServiceBenefitType> ecosystemServiceBenefitType;

    public static volatile SingularAttribute<EcosystemServiceIndicator,Long> id;

    public static volatile SingularAttribute<EcosystemServiceIndicator,String> minimumMappingUnit;
    
    public static volatile SingularAttribute<EcosystemServiceIndicator,String> dataSources;

    public static volatile SingularAttribute<EcosystemServiceIndicator,String> spatialResolution;

    public static volatile SingularAttribute<EcosystemServiceIndicator,Long> endYear;

    public static volatile SingularAttribute<EcosystemServiceIndicator,String> kml;

    public static volatile SingularAttribute<EcosystemServiceIndicator,String> comments;

    public static volatile SingularAttribute<EcosystemServiceIndicator,Long> startYear;

    public static volatile SetAttribute<EcosystemServiceIndicator,Biome> biomes;

    public static volatile SetAttribute<EcosystemServiceIndicator,Grouping> groupings;
    
    public static volatile SingularAttribute<EcosystemServiceIndicator, IndicatorSurface> indicatorSurface;

}

