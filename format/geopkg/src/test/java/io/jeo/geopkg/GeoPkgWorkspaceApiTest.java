package io.jeo.geopkg;

import io.jeo.Tests;
import io.jeo.data.Workspace;
import io.jeo.data.WorkspaceApiTestBase;

import java.io.File;

public class GeoPkgWorkspaceApiTest extends WorkspaceApiTestBase {
    @Override
    protected Workspace createWorkspace() throws Exception {
        File dir = Tests.newTmpDir("work", "gpkg");

        return GeoPackage.open(new File(dir, "test.gpkg"));
    }
}
