package org.esp.upload;

import it.jrc.auth.RoleManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;





import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.vaadin.ui.UI;

import static org.mockito.Mockito.*;

public class TestPersistModule extends AbstractModule {
  
  @Override
  protected void configure() {
    
    bind(UI.class).toInstance(mock(UI.class));
    bind(RoleManager.class).toInstance(mock(RoleManager.class));
    
    EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("esp-domain");
    bind(EntityManagerFactory.class).toInstance(entityManagerFactory);
    
    bind(EntityManager.class).toInstance(entityManagerFactory.createEntityManager());
    
    
    /*
     * Bind constants
     */
    Names.bindProperties(binder(), getProperties());
  }

  private Properties getProperties() {
    Properties props = new Properties();
    InputStream stream = this.getClass().getClassLoader().getResourceAsStream("runtime.properties");
    try {
      props.load(stream);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return props;
  }
  
}
