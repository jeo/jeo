package io.jeo.geopkg;

import io.jeo.Tests;
import io.jeo.data.Workspace;
import io.jeo.data.WorkspaceApiTestBase;

import java.io.File;
import java.nio.file.Path;

public class GeoPkgWorkspaceApiTest extends WorkspaceApiTestBase {
    @Override
    protected Workspace createWorkspace() throws Exception {
        Path dir = Tests.newTmpDir("work", "gpkg");

        return GeoPackage.open(dir.resolve("test.gpkg"));
    }
}
