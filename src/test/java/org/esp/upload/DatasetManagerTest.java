package org.esp.upload;

import java.io.File;

import net.lingala.zip4j.exception.ZipException;

import org.junit.Test;

public class DatasetManagerTest {

    DatasetManager dsm = new DatasetManager();
    
    File testFile = new File("src/test/Timber.zip");

//    @Test
    public void testShp() throws ZipException {


        File f = dsm.unzip(testFile);

        String[] fileNames = f.list();
        for (String fn : fileNames) {
            System.out.println(fn);
        }
        
        
        File newF = dsm.renameShp(f, "esp-888");
        System.out.println("new zip: " + newF.getAbsolutePath());

    }
    

}
