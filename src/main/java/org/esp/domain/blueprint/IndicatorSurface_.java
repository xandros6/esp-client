package org.esp.domain.blueprint;

import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

import com.vividsolutions.jts.geom.Polygon;

@StaticMetamodel(IndicatorSurface.class)
public abstract class IndicatorSurface_ {

    public static volatile SingularAttribute<IndicatorSurface,EcosystemServiceIndicator> ecosystemServiceIndicator;

    public static volatile SingularAttribute<IndicatorSurface,SpatialDataType> spatialDataType;

    public static volatile SingularAttribute<IndicatorSurface,Double> maxVal;

    public static volatile SingularAttribute<IndicatorSurface,Polygon> envelope;

    public static volatile SingularAttribute<IndicatorSurface,String> layerName;

    public static volatile SingularAttribute<IndicatorSurface,String> srid;

    public static volatile SingularAttribute<IndicatorSurface,Double> pixelSizeY;

    public static volatile SingularAttribute<IndicatorSurface,Double> pixelSizeX;

    public static volatile SingularAttribute<IndicatorSurface,Double> minVal;

    public static volatile SingularAttribute<IndicatorSurface,Long> id;
}
