package org.esp.domain.blueprint;

import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(ClassificationSystem.class)
public abstract class ClassificationSystem_ {

    public static volatile SingularAttribute<ClassificationSystem,String> label;

    public static volatile SingularAttribute<ClassificationSystem,Long> id;
}
