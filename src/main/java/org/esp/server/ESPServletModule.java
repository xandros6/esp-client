package org.esp.server;

import org.apache.shiro.realm.Realm;
import org.esp.ui.AppUI;
import org.jrc.auth.AuthFilter;
import org.jrc.auth.AuthServlet;
import org.jrc.auth.FakeAuthServlet;
import org.jrc.auth.JpaRealm;
import org.jrc.auth.SecurityFilter;
import org.jrc.inject.AbstractGuiceServletModule;
import org.jrc.inject.GuiceApplicationServlet;
import com.google.code.vaadin.application.MVPApplicationInitParameters;
import com.google.inject.name.Names;
import com.google.inject.persist.PersistFilter;

/**
 * 
 * @author Will Temperley
 *
 */
public class ESPServletModule extends AbstractGuiceServletModule {


    @Override
    protected void configureServlets() {
        
        bind(Class.class).annotatedWith(Names.named(MVPApplicationInitParameters.P_APPLICATION_UI_CLASS)).toInstance(AppUI.class);
        
        bind(Realm.class).to(JpaRealm.class);

        /*
         * Persistence objects
         */
//        bind(Dao.class).in(SessionScoped.class);

        /*
         * Bind constants
         */
        Names.bindProperties(binder(), getProperties());

        /*
         * Security and persistence filters
         */
        filter("/*").through(PersistFilter.class);

        filter("/*").through(SecurityFilter.class, getIni());
        
        filter("/*").through(AuthFilter.class);
        
//        serve("/login").with(AuthServlet.class);
        serve("/login").with(FakeAuthServlet.class);

        /*
         * Main application srvlet
         */
        serve("/*").with(GuiceApplicationServlet.class, getServletParams());

    }

}