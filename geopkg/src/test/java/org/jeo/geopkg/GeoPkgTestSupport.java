package org.jeo.geopkg;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

import jsqlite.Database;

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
        File dir = File.createTempFile("geopkg", name.getMethodName(), new File("target"));
        dir.delete();
        dir.mkdirs();
        return dir;
    }

    protected File unzip(final InputStream archive, final File dir) 
        throws ZipException, IOException {

        ZipInputStream zipin = new ZipInputStream(archive);
        ZipEntry zipEntry = null;

        while((zipEntry = zipin.getNextEntry()) != null) {
            if (zipEntry.isDirectory())
                continue;

            final File targetFile = new File(dir, zipEntry.getName());
            Files.createParentDirs(targetFile);

            ByteStreams.copy(zipin, Files.newOutputStreamSupplier(targetFile).getOutput());
        }

        zipin.close();
        return dir;
    }
}
