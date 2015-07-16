package io.jeo.mongo;

import com.mongodb.DB;
import io.jeo.data.Workspace;
import io.jeo.data.WorkspaceApiTestBase;
import org.junit.BeforeClass;

public class MongoWorkspaceApiTest extends WorkspaceApiTestBase {

    static DB db;

    @BeforeClass
    public static void connect() {
        db = MongoTests.connect();
    }

    @Override
    protected Workspace createWorkspace() throws Exception {
        return new MongoWorkspace(db);
    }
}
