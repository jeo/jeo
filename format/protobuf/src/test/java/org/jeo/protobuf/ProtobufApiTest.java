package org.jeo.protobuf;

import java.io.File;

import org.jeo.Tests;
import org.jeo.data.VectorApiTestBase;
import org.jeo.data.VectorData;

public class ProtobufApiTest extends VectorApiTestBase {

    @Override
    protected VectorData createVectorData() throws Exception {
        File f = Tests.gunzip(getClass().getResourceAsStream("states.pbf.gz"), 
            Tests.newTmpDir("states", "pbf"), "states.pbf");
        return new ProtobufDataset(f);
    }

}
