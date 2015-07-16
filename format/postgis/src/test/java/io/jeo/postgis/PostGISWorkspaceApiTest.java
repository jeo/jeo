package io.jeo.postgis;

import io.jeo.data.Workspace;
import io.jeo.data.WorkspaceApiTestBase;
import org.junit.BeforeClass;

public class PostGISWorkspaceApiTest extends WorkspaceApiTestBase {

    @BeforeClass
    public static void connect() {
        PostGISTests.connect();
    }

    @Override
    protected Workspace createWorkspace() throws Exception {
        return new PostGISWorkspace(PostGISTests.OPTS);
    }
}
