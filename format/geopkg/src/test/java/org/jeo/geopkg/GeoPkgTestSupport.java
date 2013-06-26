package org.jeo.geopkg;

import java.io.File;
import java.io.IOException;

import org.jeo.Tests;
import org.junit.Rule;
import org.junit.rules.TestName;

public class GeoPkgTestSupport {

    //uncomment to view debug logs during test
//    @org.junit.BeforeClass
//    public static void logging() {
//      
//      java.util.logging.Logger log = java.util.logging.Logger.getLogger(
//          org.slf4j.LoggerFactory.getLogger(GeoPackage.class).getName());
//      log.setLevel(java.util.logging.Level.FINE);
//    
//      java.util.logging.ConsoleHandler h = new java.util.logging.ConsoleHandler();
//      h.setLevel(java.util.logging.Level.FINE);
//      log.addHandler(h);
//    }

    @Rule public TestName name = new TestName();

    protected File newTmpDir() throws IOException {
        return Tests.newTmpDir("geopkg", name.getMethodName());
    }
}
