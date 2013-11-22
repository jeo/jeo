package org.jeo.geopkg;

import java.io.File;

import org.jeo.Tests;
import org.jeo.data.VectorApiTestBase;
import org.jeo.data.VectorDataset;

public class GeoPkgVectorApiTest extends VectorApiTestBase {

    @Override
    protected VectorDataset createVectorData() throws Exception {
        File dir = Tests.newTmpDir("gpkg", "states");
        Tests.unzip(getClass().getResourceAsStream("usa.gpkg.zip"), dir);

        GeoPkgWorkspace gpkg = GeoPackage.open(new File(dir, "usa.gpkg"));
        return (VectorDataset) gpkg.get("states");
    }

}
