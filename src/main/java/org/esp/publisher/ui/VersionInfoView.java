package org.esp.publisher.ui;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;

/**
 * @author Will Temperley
 *
 *         A hidden view used to determine the exact build that's running, for
 *         diagnostic purposes.
 *
 */
public class VersionInfoView extends CssLayout implements View {

	@Inject
	public VersionInfoView(@Named("build_date") String buildDate) {
		addComponent(new Label(buildDate));
	}

	@Override
	public void enter(ViewChangeEvent event) {

	}

}
