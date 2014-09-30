package org.esp.domain.blueprint;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(FileType.class)
public abstract class FileType_ {

    public static volatile SingularAttribute<FileType,String> label;

    public static volatile SingularAttribute<FileType,Long> id;
}
