package org.jeo.postgis;

import org.jeo.data.VectorApiTestBase;
import org.jeo.data.VectorDataset;
import org.junit.After;
import org.junit.Assume;
import org.junit.BeforeClass;

public class PostGISApiTest extends VectorApiTestBase {

    @BeforeClass
    public static void connect()  {
        try {
            PostGISTests.connect();
        }
        catch(Exception e) {
            Assume.assumeTrue(false);
        }
    }

    PostGISWorkspace pg;

    @Override
    protected void init() throws Exception {
        pg = new PostGISWorkspace(PostGISTests.OPTS);
    }

    @Override
    protected VectorDataset createVectorData() throws Exception {
        return pg.get("states");
    }

    @After
    public void tearDown() {
        pg.close();
    }
}
