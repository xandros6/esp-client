package org.esp.domain.blueprint;

import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(ColourMapEntry.class)
public abstract class ColourMapEntry_ {

    public static volatile SingularAttribute<ColourMapEntry,ColourMap> colourMap;

    public static volatile SingularAttribute<ColourMapEntry,Double> value;

    public static volatile SingularAttribute<ColourMapEntry,String> label;

    public static volatile SingularAttribute<ColourMapEntry,Long> green;

    public static volatile SingularAttribute<ColourMapEntry,Long> blue;

    public static volatile SingularAttribute<ColourMapEntry,Long> id;

    public static volatile SingularAttribute<ColourMapEntry,Long> red;

    public static volatile SingularAttribute<ColourMapEntry,Long> alpha;
}
