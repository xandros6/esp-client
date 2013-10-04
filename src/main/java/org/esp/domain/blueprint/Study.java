package org.esp.domain.blueprint;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.jrc.auth.domain.Role;

@Entity
@Table(schema = "blueprint", name = "study")
public class Study {

    private Long id;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "seq")
    @SequenceGenerator(allocationSize = 1, name = "seq", sequenceName = "blueprint.study_id_seq")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    private Role role;

    @ManyToOne
    @NotNull
    @JoinColumn(name="role_id")
    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    private ProjectType projectType;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "project_type_id")
    public ProjectType getProjectType() {
        return projectType;
    }

    public void setProjectType(ProjectType projectType) {
        this.projectType = projectType;
    }

    private StudyPurpose studyPurpose;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "study_purpose_id")
    public StudyPurpose getStudyPurpose() {
        return studyPurpose;
    }

    public void setStudyPurpose(StudyPurpose studyPurpose) {
        this.studyPurpose = studyPurpose;
    }

    private String keywords;

    @NotNull
    @Column
    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    private String studyName;

    @NotNull
    @Column(name = "study_name")
    public String getStudyName() {
        return studyName;
    }

    public void setStudyName(String studyName) {
        this.studyName = studyName;
    }

    private String studyLocation;

    @Column(name = "study_location")
    public String getStudyLocation() {
        return studyLocation;
    }

    public void setStudyLocation(String studyLocation) {
        this.studyLocation = studyLocation;
    }
    
    private String studyDuration;

    @Column(name="study_duration")
    public String getStudyDuration() {
        return studyDuration;
    }

    public void setStudyDuration(String studyDuration) {
        this.studyDuration = studyDuration;
    }

    private String mainInvestigators;

    @NotNull
    @Column(name = "main_investigators")
    public String getMainInvestigators() {
        return mainInvestigators;
    }

    public void setMainInvestigators(String mainInvestigators) {
        this.mainInvestigators = mainInvestigators;
    }

    private String projectReferences;

    @NotNull
    @Column(name = "project_references")
    public String getProjectReferences() {
        return projectReferences;
    }

    public void setProjectReferences(String projectReferences) {
        this.projectReferences = projectReferences;
    }

    private String contactDetails;

    @Column(name = "contact_details")
    public String getContactDetails() {
        return contactDetails;
    }

    public void setContactDetails(String contactDetails) {
        this.contactDetails = contactDetails;
    }

    private String fundingSource;

    @Column(name = "funding_source")
    public String getFundingSource() {
        return fundingSource;
    }

    public void setFundingSource(String fundingSource) {
        this.fundingSource = fundingSource;
    }

    private Set<EcosystemServiceIndicator> ecosystemServiceIndicators;

    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL)
    public Set<EcosystemServiceIndicator> getEcosystemServiceIndicators() {
        return ecosystemServiceIndicators;
    }

    public void setEcosystemServiceIndicators(
            Set<EcosystemServiceIndicator> ecosystemServiceIndicators) {
        this.ecosystemServiceIndicators = ecosystemServiceIndicators;
    }

    @Override
    public String toString() {
        return studyName;
    }
    
    @Override
    public int hashCode() {
        return id.intValue();
    }
    
}
