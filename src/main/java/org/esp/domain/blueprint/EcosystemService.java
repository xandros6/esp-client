package org.esp.domain.blueprint;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;

@Entity
@Table(schema = "blueprint", name = "ecosystem_service")
public class EcosystemService {

    private Long id;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "seq")
    @SequenceGenerator(allocationSize = 1, name = "seq", sequenceName = "blueprint.ecosystem_service_id_seq")
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    private String label;

    @Column
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    private String description;

    @Column
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    private EcosystemServiceCategory ecosystemServiceCategory;

    @ManyToOne
    @JoinColumn(name="ecosystem_service_category_id")
    public EcosystemServiceCategory getEcosystemServiceCategory() {
        return ecosystemServiceCategory;
    }

    public void setEcosystemServiceCategory(EcosystemServiceCategory ecosystemServiceCategory) {
        this.ecosystemServiceCategory = ecosystemServiceCategory;
    }

    private ClassificationSystem classificationSystem;

    @ManyToOne
    @JoinColumn(name="classification_system_id")
    public ClassificationSystem getClassificationSystem() {
        return classificationSystem;
    }

    public void setClassificationSystem(ClassificationSystem classificationSystem) {
        this.classificationSystem = classificationSystem;
    }

    private String iconUrl;

    @Column(name="icon_url")
    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    @Override
    public String toString() {
        return label;
    }
}
