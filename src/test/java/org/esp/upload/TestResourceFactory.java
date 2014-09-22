package org.esp.upload;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.esp.publisher.ui.AppUI;
import org.esp.publisher.ui.ViewModule;
import org.vaadin.addons.guice.uiscope.UIScopeModule;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class TestResourceFactory {

    public static File baseDir = new File("src/test");

    public static InputStream getFileInputStream(String filename)
            throws FileNotFoundException {
        File f = new File(baseDir, filename);
        FileInputStream fis = new FileInputStream(f);
        return fis;
    }

    public static Injector getInjector() {
        Injector injector = Guice.createInjector(new TestPersistModule(), new ViewModule(),
                new UIScopeModule(AppUI.class));
        return injector;
    }

}
