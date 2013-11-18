package org.jeo.ogr;

import java.io.File;

import org.jeo.Tests;
import org.jeo.data.VectorApiTestBase;
import org.jeo.data.VectorDataset;
import org.junit.BeforeClass;

public class ShapefileApiTest extends VectorApiTestBase {

    @BeforeClass
    public static void initOGR() {
        OGRTest.initOGR();
    }

    @Override
    protected VectorDataset createVectorData() throws Exception {
        File dir = Tests.newTmpDir("shp", "data");
        Tests.unzip(getClass().getResourceAsStream("states.zip"), dir);

        return Shapefile.open(new File(dir, "states.shp"));
    }

}
