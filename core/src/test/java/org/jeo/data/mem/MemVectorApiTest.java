package org.jeo.data.mem;

import org.jeo.TestData;
import org.jeo.data.VectorApiTestBase;
import org.jeo.data.VectorData;

public class MemVectorApiTest extends VectorApiTestBase {

    @Override
    protected VectorData createVectorData() {
        return TestData.states();
    }

}
