package org.esp.domain.blueprint;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.JoinTable;
import javax.persistence.CascadeType;

import org.jrc.persist.adminunits.Country;
import org.jrc.persist.adminunits.Grouping;

import java.util.Set;

@Entity
@Table(schema = "blueprint", name = "ecosystem_service_indicator")
public class EcosystemServiceIndicator {

    private Long id;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "seq")
    @SequenceGenerator(allocationSize = 1, name = "seq", sequenceName = "blueprint.ecosystem_service_indicator_id_seq")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    private EcosystemService ecosystemService;

    @ManyToOne
    @JoinColumn(name = "ecosystem_service_id")
    public EcosystemService getEcosystemService() {
        return ecosystemService;
    }

    public void setEcosystemService(EcosystemService ecosystemService) {
        this.ecosystemService = ecosystemService;
    }

    private Study study;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "study_id")
    public Study getStudy() {
        return study;
    }

    public void setStudy(Study study) {
        this.study = study;
    }

    private StudyObjectiveMet studyObjectiveMet;

    @ManyToOne
    @JoinColumn(name = "study_objective_met_id")
    public StudyObjectiveMet getStudyObjectiveMet() {
        return studyObjectiveMet;
    }

    public void setStudyObjectiveMet(StudyObjectiveMet studyObjectiveMet) {
        this.studyObjectiveMet = studyObjectiveMet;
    }

    private SpatialLevel spatialLevel;

    @ManyToOne
    @JoinColumn(name = "spatial_level_id")
    public SpatialLevel getSpatialLevel() {
        return spatialLevel;
    }

    public void setSpatialLevel(SpatialLevel spatialLevel) {
        this.spatialLevel = spatialLevel;
    }

    private QuantificationUnit quantificationUnit;

    @ManyToOne
    @JoinColumn(name = "quantification_unit_id")
    public QuantificationUnit getQuantificationUnit() {
        return quantificationUnit;
    }

    public void setQuantificationUnit(QuantificationUnit quantificationUnit) {
        this.quantificationUnit = quantificationUnit;
    }

    private ArealUnit arealUnit;

    @ManyToOne
    @JoinColumn(name = "areal_unit_id")
    public ArealUnit getArealUnit() {
        return arealUnit;
    }

    public void setArealUnit(ArealUnit arealUnit) {
        this.arealUnit = arealUnit;
    }

    private Indicator indicator;

    @ManyToOne
    @JoinColumn(name = "indicator_id")
    public Indicator getIndicator() {
        return indicator;
    }

    public void setIndicator(Indicator indicator) {
        this.indicator = indicator;
    }

    private TemporalUnit temporalUnit;

    @ManyToOne
    @JoinColumn(name = "temporal_unit_id")
    public TemporalUnit getTemporalUnit() {
        return temporalUnit;
    }

    public void setTemporalUnit(TemporalUnit temporalUnit) {
        this.temporalUnit = temporalUnit;
    }

    private QuantificationMethod quantificationMethod;

    @ManyToOne
    @JoinColumn(name = "quantification_method_id")
    public QuantificationMethod getQuantificationMethod() {
        return quantificationMethod;
    }

    public void setQuantificationMethod(
            QuantificationMethod quantificationMethod) {
        this.quantificationMethod = quantificationMethod;
    }

    private SpatialDataType spatialDataType;

    @ManyToOne
    @JoinColumn(name = "spatial_data_type_id")
    public SpatialDataType getSpatialDataType() {
        return spatialDataType;
    }

    public void setSpatialDataType(SpatialDataType spatialDataType) {
        this.spatialDataType = spatialDataType;
    }

    private EcosystemServiceAccountingType ecosystemServiceAccountingType;

    @ManyToOne
    @JoinColumn(name = "ecosystem_service_accounting_type_id")
    public EcosystemServiceAccountingType getEcosystemServiceAccountingType() {
        return ecosystemServiceAccountingType;
    }

    public void setEcosystemServiceAccountingType(
            EcosystemServiceAccountingType ecosystemServiceAccountingType) {
        this.ecosystemServiceAccountingType = ecosystemServiceAccountingType;
    }

    private EcosystemServiceBenefitType ecosystemServiceBenefitType;

    @ManyToOne
    @JoinColumn(name = "ecosystem_service_benefit_type_id")
    public EcosystemServiceBenefitType getEcosystemServiceBenefitType() {
        return ecosystemServiceBenefitType;
    }

    public void setEcosystemServiceBenefitType(
            EcosystemServiceBenefitType ecosystemServiceBenefitType) {
        this.ecosystemServiceBenefitType = ecosystemServiceBenefitType;
    }

    private String minimumMappingUnit;

    @Column(name = "minimum_mapping_unit")
    public String getMinimumMappingUnit() {
        return minimumMappingUnit;
    }

    public void setMinimumMappingUnit(String minimumMappingUnit) {
        this.minimumMappingUnit = minimumMappingUnit;
    }

    private String spatialResolution;

    @Column(name = "spatial_resolution")
    public String getSpatialResolution() {
        return spatialResolution;
    }

    public void setSpatialResolution(String spatialResolution) {
        this.spatialResolution = spatialResolution;
    }

    private Long endYear;

    @Column(name = "end_year")
    public Long getEndYear() {
        return endYear;
    }

    public void setEndYear(Long endYear) {
        this.endYear = endYear;
    }

    private String kml;

    @Column
    public String getKml() {
        return kml;
    }

    public void setKml(String kml) {
        this.kml = kml;
    }

    private String comments;

    @Column
    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    private Long startYear;

    @Column(name = "start_year")
    public Long getStartYear() {
        return startYear;
    }

    public void setStartYear(Long startYear) {
        this.startYear = startYear;
    }

    private Set<Biome> biomes;

    @ManyToMany
    @JoinTable(name = "blueprint.ecosystem_service_indicator_biome", joinColumns = @JoinColumn(name = "ecosystem_service_indicator_id"), inverseJoinColumns = @JoinColumn(name = "biome_id"))
    public Set<Biome> getBiomes() {
        return biomes;
    }

    public void setBiomes(Set<Biome> biomes) {
        this.biomes = biomes;
    }

    private Set<Grouping> groupings;

    @ManyToMany
    @JoinTable(name = "blueprint.ecosystem_service_indicator_grouping", joinColumns = @JoinColumn(name = "ecosystem_service_indicator_id"), inverseJoinColumns = @JoinColumn(name = "grouping_id"))
    public Set<Grouping> getGroupings() {
        return groupings;
    }

    public void setGroupings(Set<Grouping> groupings) {
        this.groupings = groupings;
    }

    private IndicatorSurface indicatorSurface;

    @OneToOne(mappedBy = "ecosystemServiceIndicator", cascade=CascadeType.ALL)
    public IndicatorSurface getIndicatorSurface() {
        return indicatorSurface;
    }

    public void setIndicatorSurface(IndicatorSurface indicatorSurface) {
        this.indicatorSurface = indicatorSurface;
    }
    
    private String dataSources;

    @Column(name = "data_sources")
    public String getDataSources() {
        return dataSources;
    }

    public void setDataSources(String dataSources) {
        this.dataSources = dataSources;
    }

    @Override
    public String toString() {
        return String.format("%s - %s", ecosystemService, indicator);
    }

}
