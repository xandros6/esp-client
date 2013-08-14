package org.esp.domain.blueprint;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(StudyObjectiveMet.class)
public abstract class StudyObjectiveMet_ {

    public static volatile SingularAttribute<StudyObjectiveMet,String> label;

    public static volatile SingularAttribute<StudyObjectiveMet,Long> id;
}
