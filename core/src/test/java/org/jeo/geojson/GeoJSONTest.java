package org.jeo.geojson;

import java.io.File;

import org.jeo.Tests;
import org.jeo.data.VectorApiTestBase;
import org.jeo.data.VectorDataset;

public class GeoJSONTest extends VectorApiTestBase {

    @Override
    protected VectorDataset createVectorData() throws Exception {
        File dir = Tests.unzip(getClass().getResourceAsStream("states.zip"), Tests.newTmpDir());
        return new GeoJSONDataset(new File(dir, "states.json"));
    }

}
