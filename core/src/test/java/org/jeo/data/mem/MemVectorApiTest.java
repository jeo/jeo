package org.jeo.data.mem;

import org.jeo.TestData;
import org.jeo.data.VectorApiTestBase;
import org.jeo.data.VectorDataset;

public class MemVectorApiTest extends VectorApiTestBase {

    @Override
    protected VectorDataset createVectorData() {
        return TestData.states();
    }

}
