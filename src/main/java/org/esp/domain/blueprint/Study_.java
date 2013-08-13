package org.esp.domain.blueprint;

import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

import org.jrc.auth.domain.Role;

@StaticMetamodel(Study.class)
public abstract class Study_ {

    public static volatile SingularAttribute<Study, Role> role;

    public static volatile SingularAttribute<Study, ProjectType> projectType;

    public static volatile SingularAttribute<Study, StudyPurpose> studyPurpose;

    public static volatile SingularAttribute<Study, String> studyDuration;

    public static volatile SingularAttribute<Study, String> keywords;

    public static volatile SingularAttribute<Study, String> studyName;

    public static volatile SingularAttribute<Study, String> studyLocation;

    public static volatile SingularAttribute<Study, String> mainInvestigators;

    public static volatile SingularAttribute<Study, String> projectReferences;

    public static volatile SingularAttribute<Study, String> contactDetails;

    public static volatile SingularAttribute<Study, Long> id;

    public static volatile SingularAttribute<Study, String> fundingSource;

    public static volatile SetAttribute<Study, EcosystemServiceIndicator> ecosystemServiceIndicators;
}
