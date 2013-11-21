package org.jeo.geopkg;

import java.io.File;

import org.jeo.Tests;
import org.jeo.data.VectorApiTestBase;
import org.jeo.data.VectorDataset;

public class GeoPkgVectorApiTest extends VectorApiTestBase {

    @Override
    protected VectorDataset createVectorData() throws Exception {
        File dir = Tests.newTmpDir("gpkg", "states");
        Tests.unzip(getClass().getResourceAsStream("states.geopackage.zip"), dir);

        GeoPkgWorkspace gpkg = GeoPackage.open(new File(dir, "states.geopackage"));
        return (VectorDataset) gpkg.get("states");
    }

}
