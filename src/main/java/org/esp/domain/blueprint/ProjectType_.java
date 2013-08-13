package org.esp.domain.blueprint;

import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(ProjectType.class)
public abstract class ProjectType_ {

    public static volatile SingularAttribute<ProjectType,Long> id;

    public static volatile SingularAttribute<ProjectType,String> label;
}
