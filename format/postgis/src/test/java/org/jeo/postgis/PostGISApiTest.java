package org.jeo.postgis;

import java.io.IOException;

import org.jeo.data.VectorApiTestBase;
import org.jeo.data.VectorData;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
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
    protected VectorData createVectorData() throws Exception {
        return pg.get("states");
    }

    @After
    public void tearDown() {
        pg.close();
    }
}
