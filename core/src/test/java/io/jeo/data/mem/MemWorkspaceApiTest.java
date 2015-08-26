package io.jeo.data.mem;

import io.jeo.data.Workspace;
import io.jeo.data.WorkspaceApiTestBase;

public class MemWorkspaceApiTest extends WorkspaceApiTestBase {
    @Override
    protected Workspace createWorkspace() throws Exception {
        return Memory.open("test");
    }
}
