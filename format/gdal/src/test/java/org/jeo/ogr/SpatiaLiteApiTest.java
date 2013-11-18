package org.jeo.ogr;

import java.io.File;

import org.jeo.Tests;
import org.jeo.data.VectorApiTestBase;
import org.jeo.data.VectorDataset;
import org.junit.BeforeClass;

public class SpatiaLiteApiTest extends VectorApiTestBase {

    @BeforeClass
    public static void initOGR() {
        OGRTest.initOGR();
    }

    @Override
    protected VectorDataset createVectorData() throws Exception {
        File dir = Tests.newTmpDir("spatialite", "data");
        Tests.unzip(getClass().getResourceAsStream("usa.db.zip"), dir);

        OGRWorkspace ws = SpatiaLite.open(new File(dir, "usa.db"));
        return ws.get("states");
    }

}
