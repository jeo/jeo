package org.jeo.geopkg;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.jeo.data.Workspaces;

import org.junit.Before;
import org.junit.Test;

public class GeoPkgTest extends GeoPkgTestSupport {


    @Before
    public void setUp() throws ClassNotFoundException {
        Class.forName(GeoPackage.class.getCanonicalName());
    }

    @Test
    public void testCreateFromFactory() throws Exception {
        assertNull(Workspaces.create(newFile("foo.bar")));
        assertNotNull(Workspaces.create(newFile("foo.geopkg")));
    }

    File newFile(String name) throws IOException {
        File f = new File(new File("target"), name);
        if (f.exists()) {
            f.delete();
        }
        f.createNewFile();
        return f;
    }
}
