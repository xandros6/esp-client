package org.esp.editor;

import java.util.Set;

import org.esp.domain.blueprint.EcosystemServiceIndicator;
import org.esp.domain.blueprint.IndicatorSurface;
import org.esp.domain.blueprint.Study;
import org.esp.domain.blueprint.Study_;
import org.esp.ui.ViewModule;
import org.esp.upload.GeoserverRest;
import org.jrc.form.editor.BaseEditor;
import org.jrc.persist.Dao;

import com.google.inject.Inject;

public class InlineStudyEditor extends BaseEditor<Study> {

    private GeoserverRest gsr;

    @Inject
    public InlineStudyEditor(final Dao dao, GeoserverRest gsr) {

        super(Study.class, dao);

        this.gsr = gsr;

        ff.addField(Study_.studyName);
        ff.addField(Study_.studyPurpose);
        ff.addField(Study_.studyLocation);
        ff.addField(Study_.studyDuration);
        ff.addField(Study_.projectType);
        ff.addTextArea(Study_.projectReferences);
        ff.addTextArea(Study_.contactDetails);
        ff.addTextArea(Study_.fundingSource);
        ff.addTextArea(Study_.keywords);
        ff.addTextArea(Study_.mainInvestigators);

        addFieldGroup("Study");

    }

    @Override
    protected void doPostDelete(Study entity) {

        Set<EcosystemServiceIndicator> esis = entity
                .getEcosystemServiceIndicators();
        if (esis != null) {
            for (EcosystemServiceIndicator ecosystemServiceIndicator : esis) {
                IndicatorSurface surface = ecosystemServiceIndicator
                        .getIndicatorSurface();
                if (surface != null) {
                    gsr.removeStore(surface);
                }
            }
        }
        getUI().getCurrent().getNavigator().navigateTo(ViewModule.HOME);
    }

}
