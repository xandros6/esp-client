package org.esp.domain.blueprint;

import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(StudyPurpose.class)
public abstract class StudyPurpose_ {

    public static volatile SingularAttribute<StudyPurpose,String> label;

    public static volatile SingularAttribute<StudyPurpose,Long> id;
}
