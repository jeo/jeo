package org.jeo.ogr;

import static org.junit.Assert.*;

import java.io.File;

import org.jeo.Tests;
import org.jeo.data.Drivers;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class OGRTest {

    File data;

    @BeforeClass
    public static void initOGR() {
        try {
            OGR.init();
        }
        catch(Throwable e) {
            Assume.assumeTrue(false);
        }
    }

    @Before
    public void setUp() throws Exception {
        File dir = Tests.newTmpDir("ogr", "data");
        Tests.unzip(getClass().getResourceAsStream("states.zip"), dir);

        data = new File(dir, "states.shp");
    }

    @Test
    public void testDriverFind() throws Exception {
        assertTrue(OGR.class.isInstance(Drivers.find("ogr")));
        assertTrue(Shapefile.class.isInstance(Drivers.find("shp")));
    }

    @Test
    public void testDriverOpen() throws Exception {
        assertTrue(OGRDataset.class.isInstance(Drivers.open(data)));
    }
}
