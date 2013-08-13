package org.esp.server;

import org.esp.ui.ViewModule;
import org.jrc.inject.MvpModule;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.jpa.JpaPersistModule;
import com.google.inject.servlet.GuiceServletContextListener;

public class GuiceContextListener extends GuiceServletContextListener {
	@Override
	protected Injector getInjector() {
		return Guice.createInjector(new ESPServletModule(), new ViewModule(), new MvpModule(), new JpaPersistModule("esp-domain"));
	}
}
