package org.esp.domain.blueprint;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;

import org.esp.domain.publisher.ColourMap;
import org.hibernate.annotations.Type;

import com.vividsolutions.jts.geom.Polygon;

@Entity
@Table(schema = "blueprint", name = "indicator_surface")
public class IndicatorSurface {

    private Long id;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "seq")
    @SequenceGenerator(allocationSize = 1, name = "seq", sequenceName = "blueprint.indicator_surface_id_seq")
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    private EcosystemServiceIndicator ecosystemServiceIndicator;

    @OneToOne(cascade= CascadeType.ALL)
    @JoinColumn(name="ecosystem_service_indicator_id")
    public EcosystemServiceIndicator getEcosystemServiceIndicator() {
        return ecosystemServiceIndicator;
    }

    public void setEcosystemServiceIndicator(EcosystemServiceIndicator ecosystemServiceIndicator) {
        this.ecosystemServiceIndicator = ecosystemServiceIndicator;
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
    @JoinColumn(name="colour_map_id")
    public ColourMap getColourMap() {
        return colourMap;
    }

    public void setColourMap(ColourMap colourMap) {
        this.colourMap = colourMap;
    }

    @Override
    public int hashCode() {
        if (id != null) {
            return id.intValue();
        }
        return super.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        
        if (obj instanceof IndicatorSurface) {
            IndicatorSurface comparee = (IndicatorSurface) obj;
            if (comparee.getId().equals(getId())) {
                return true;
            }
            return false;
        }
        return super.equals(obj);
    }
    
}
