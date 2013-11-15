package org.jeo.ogr;

import java.io.File;

import org.jeo.Tests;
import org.jeo.data.VectorApiTestBase;
import org.jeo.data.VectorDataset;
import org.junit.Assume;
import org.junit.BeforeClass;

public class OGRApiTest extends VectorApiTestBase {

    @BeforeClass
    public static void initOGR() {
        try {
            OGR.init();
        }
        catch(Throwable e) {
            Assume.assumeTrue(false);
        }
    }

    @Override
    protected VectorDataset createVectorData() throws Exception {
        File dir = Tests.newTmpDir("ogr", "data");
        Tests.unzip(getClass().getResourceAsStream("states.zip"), dir);

        return Shapefile.open(new File(dir, "states.shp"));
    }

}
