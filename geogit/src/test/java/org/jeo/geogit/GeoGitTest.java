package org.jeo.geogit;

import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.geogit.api.GeoGIT;
import org.geogit.di.GeogitModule;
import org.geogit.storage.bdbje.JEStorageModule;
import org.jeo.Tests;
import org.junit.After;
import org.junit.Before;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;

public class GeoGitTest {

    GeoGIT gg;
    
    @Before
    public void setUp() throws IOException {

        Injector i = Guice.createInjector(
            Modules.override(new GeogitModule()).with(new JEStorageModule()));

        gg = new GeoGIT(i, Tests.newTmpDir("geogit", "tmp")); 
    }

    @After
    public void tearDown() throws IOException {
        gg.close();
        FileUtils.deleteDirectory(gg.getPlatform().pwd());
    }
}
