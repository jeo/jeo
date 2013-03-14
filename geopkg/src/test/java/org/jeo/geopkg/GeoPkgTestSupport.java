package org.jeo.geopkg;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

import jsqlite.Database;

import org.jeo.Tests;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;

public class GeoPkgTestSupport {

    @BeforeClass
    public static void checkdb() {
        Throwable caught = null;
        try {
            new Database();
        }
        catch(Throwable t) {
            caught = t;
        }

        Assume.assumeNoException(caught);
    }

    @Rule public TestName name = new TestName();

    protected File newTmpDir() throws IOException {
        return Tests.newTmpDir("geopkg", name.getMethodName());
    }
}
