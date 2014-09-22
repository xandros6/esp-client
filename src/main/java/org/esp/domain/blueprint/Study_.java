package org.esp.domain.blueprint;

import it.jrc.domain.auth.Role;

import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(Study.class)
public abstract class Study_ {

    public static volatile SingularAttribute<Study, Role> role;

    public static volatile SingularAttribute<Study, ProjectType> projectType;

    public static volatile SingularAttribute<Study, StudyPurpose> studyPurpose;

    public static volatile SingularAttribute<Study, Integer> endYear;

    public static volatile SingularAttribute<Study, Integer> startYear;

    public static volatile SingularAttribute<Study, String> keywords;

    public static volatile SingularAttribute<Study, String> studyName;

    public static volatile SingularAttribute<Study, String> url;

    public static volatile SingularAttribute<Study, String> studyLocation;

    public static volatile SingularAttribute<Study, String> mainInvestigators;

    public static volatile SingularAttribute<Study, String> projectReferences;

    public static volatile SingularAttribute<Study, String> contactDetails;

    public static volatile SingularAttribute<Study, Long> id;

    public static volatile SingularAttribute<Study, String> fundingSource;

    public static volatile SetAttribute<Study, EcosystemServiceIndicator> ecosystemServiceIndicators;
}
