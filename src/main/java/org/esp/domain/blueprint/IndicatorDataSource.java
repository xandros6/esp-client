package org.esp.domain.blueprint;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;

@Entity
@Table(schema = "blueprint", name = "indicator_data_source")
public class IndicatorDataSource {

    private Long id;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "seq")
    @SequenceGenerator(allocationSize = 1, name = "seq", sequenceName = "blueprint.indicator_data_source_id_seq")
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    private EcosystemServiceIndicator ecosystemServiceIndicator;

    @ManyToOne
    @JoinColumn(name="ecosystem_service_indicator_id")
    public EcosystemServiceIndicator getEcosystemServiceIndicator() {
        return ecosystemServiceIndicator;
    }

    public void setEcosystemServiceIndicator(EcosystemServiceIndicator ecosystemServiceIndicator) {
        this.ecosystemServiceIndicator = ecosystemServiceIndicator;
    }

    private DataSource dataSource;

    @ManyToOne
    @JoinColumn(name="data_source_id")
    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
