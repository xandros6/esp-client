package org.esp.domain.publisher;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(schema = "publisher", name = "colour_map")
public class ColourMap {

    private Long id;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "seq")
    @SequenceGenerator(allocationSize = 1, name = "seq", sequenceName = "publisher.colour_map_id_seq")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    private String label;

    @NotNull
    @Column
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    private String styleName;

    @Column(name = "style_name")
    @NotNull
    public String getStyleName() {
        return styleName;
    }

    public void setStyleName(String styleName) {
        this.styleName = styleName;
    }

    private List<ColourMapEntry> colourMapEntries;

    @OneToMany(mappedBy = "colourMap", cascade = CascadeType.ALL)
    public List<ColourMapEntry> getColourMapEntries() {
        return colourMapEntries;
    }

    public void setColourMapEntries(List<ColourMapEntry> colourMapEntries) {
        this.colourMapEntries = colourMapEntries;
    }

    @Override
    public String toString() {
        return label;
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

        if (obj instanceof ColourMap) {
            ColourMap comparee = (ColourMap) obj;
            if (comparee.getId().equals(getId())) {
                return true;
            }
        }
        return super.equals(obj);
    }
}
