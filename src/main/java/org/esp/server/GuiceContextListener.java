package org.esp.server;

import org.esp.publisher.ui.AppUI;
import org.esp.publisher.ui.ViewModule;
import org.vaadin.addons.guice.uiscope.UIScopeModule;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.jpa.JpaPersistModule;
import com.google.inject.servlet.GuiceServletContextListener;

public class GuiceContextListener extends GuiceServletContextListener {
	@Override
	protected Injector getInjector() {
		return Guice.createInjector(new ESPServletModule(), new ViewModule(), new UIScopeModule(AppUI.class), new JpaPersistModule("esp-domain"));
	}
}
