package org.jeo.protobuf;

import org.jeo.Tests;
import org.jeo.vector.VectorApiTestBase;
import org.jeo.vector.VectorDataset;

import java.io.File;

public class ProtobufApiTest extends VectorApiTestBase {

    @Override
    protected VectorDataset createVectorData() throws Exception {
        File f = Tests.gunzip(getClass().getResourceAsStream("states.pbf.gz"), 
            Tests.newTmpDir("states", "pbf"), "states.pbf");
        return new ProtobufDataset(f);
    }

}
