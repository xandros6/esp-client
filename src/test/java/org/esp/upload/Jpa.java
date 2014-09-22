package org.esp.upload;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.Test;

public class Jpa {
    
    @Test
    public void testJpa() {
        
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("esp-domain");
        
        EntityManager x = emf.createEntityManager();
        List a = x.createQuery("from Country").getResultList();
        for (Object object : a) {
            System.out.println(a);
        }
        
    }

}
