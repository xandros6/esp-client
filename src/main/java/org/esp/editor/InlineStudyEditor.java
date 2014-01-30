package org.esp.editor;

import org.esp.domain.blueprint.Study;
import org.esp.domain.blueprint.Study_;
import org.esp.publisher.form.EditorController;
import org.jrc.persist.Dao;

import com.google.inject.Inject;

public class InlineStudyEditor extends EditorController<Study> {


    @Inject
    public InlineStudyEditor(final Dao dao) {

        super(Study.class, dao);


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

//        Set<EcosystemServiceIndicator> esis = entity
//                .getEcosystemServiceIndicators();
//        if (esis != null) {
//            for (EcosystemServiceIndicator ecosystemServiceIndicator : esis) {
//
//                String layerName = ecosystemServiceIndicator.getLayerName();
//                if (layerName != null) {
//                    gsr.removeRasterStore(layerName);
//                }
//            }
//        }
//        getUI().getCurrent().getNavigator().navigateTo(ViewModule.HOME);
    }

}
