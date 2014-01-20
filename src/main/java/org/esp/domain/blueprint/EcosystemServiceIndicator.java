package org.esp.domain.blueprint;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.JoinTable;
import javax.persistence.CascadeType;

import org.esp.domain.publisher.ColourMap;
import org.esp.publisher.TiffMeta;
import org.hibernate.annotations.Type;
import org.jrc.persist.adminunits.Grouping;

import com.vividsolutions.jts.geom.Polygon;

import java.util.Set;

@Entity
@Table(schema = "blueprint", name = "ecosystem_service_indicator")
public class EcosystemServiceIndicator  {

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

    @NotNull
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

    @NotNull
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

    private Set<DataSource> dataSources;

    @ManyToMany
    @JoinTable(name = "blueprint.ecosystem_service_indicator_data_source", joinColumns = @JoinColumn(name = "ecosystem_service_indicator_id"), inverseJoinColumns = @JoinColumn(name = "data_source_id"))
    public Set<DataSource> getDataSources() {
        return dataSources;
    }

    public void setDataSources(Set<DataSource> dataSources) {
        this.dataSources = dataSources;
    }

    private SpatialDataType spatialDataType;

    @ManyToOne
    @JoinColumn(name="spatial_data_type_id")
    public SpatialDataType getSpatialDataType() {
        return spatialDataType;
    }

    public void setSpatialDataType(SpatialDataType spatialDataType) {
        this.spatialDataType = spatialDataType;
    }

    private Double maxVal;

    @Column(name="max_val")
    public Double getMaxVal() {
        return maxVal;
    }

    public void setMaxVal(Double maxVal) {
        this.maxVal = maxVal;
    }

    private Polygon envelope;

    @Column
    @Type(type = "org.hibernate.spatial.GeometryType")
    public Polygon getEnvelope() {
        return envelope;
    }

    public void setEnvelope(Polygon envelope) {
        this.envelope = envelope;
    }

    private String layerName;

    @Column(name="layer_name")
    public String getLayerName() {
        return layerName;
    }

    public void setLayerName(String layerName) {
        this.layerName = layerName;
    }

    private String srid;

    @Column
    public String getSrid() {
        return srid;
    }

    public void setSrid(String srid) {
        this.srid = srid;
    }

    private Double pixelSizeY;

    @Column(name="pixel_size_y")
    public Double getPixelSizeY() {
        return pixelSizeY;
    }

    public void setPixelSizeY(Double pixelSizeY) {
        this.pixelSizeY = pixelSizeY;
    }

    private Double pixelSizeX;

    @Column(name="pixel_size_x")
    public Double getPixelSizeX() {
        return pixelSizeX;
    }

    public void setPixelSizeX(Double pixelSizeX) {
        this.pixelSizeX = pixelSizeX;
    }

    private Double minVal;

    @Column(name="min_val")
    public Double getMinVal() {
        return minVal;
    }

    public void setMinVal(Double minVal) {
        this.minVal = minVal;
    }
    
    private ColourMap colourMap;

    @ManyToOne
    @JoinColumn(name = "colour_map_id")
    public ColourMap getColourMap() {
        return colourMap;
    }

    public void setColourMap(ColourMap colourMap) {
        this.colourMap = colourMap;
    }

    @Override
    public String toString() {
        return String.format("%s - %s", ecosystemService, indicator);
    }

    @Override
    public boolean equals(Object obj) {
    
        if (obj instanceof EcosystemServiceIndicator) {
            EcosystemServiceIndicator comparee = (EcosystemServiceIndicator) obj;
            if (comparee.getId().equals(getId())) {
                return true;
            }
            return false;
        }
        return super.equals(obj);
    }
    
    @Override
    public int hashCode() {
        if (id != null) {
            return id.intValue();
        }
        return super.hashCode();
    }
}