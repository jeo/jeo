package org.jeo.shp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

import org.jeo.data.Cursor;
import org.jeo.feature.Feature;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.MultiPolygon;

public class ShapefileTest {

    @Rule public TestName name = new TestName();

    Shapefile shp;

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

    @Before
    public void setUp() throws Exception {
        File dir = unzip(getClass().getResourceAsStream("states.shp.zip"), newTmpDir());
        shp = new Shapefile(new File(dir, "states.shp"));
    }

    @Test
    public void testCountAll() throws IOException {
        assertEquals(49, shp.count(null));
    }

    @Test
    public void testBounds() throws IOException {
        Envelope e = shp.bounds();
        assertEquals(-124.73, e.getMinX(), 0.01);
        assertEquals(24.96, e.getMinY(), 0.01);
        assertEquals(-66.97, e.getMaxX(), 0.01);
        assertEquals(49.37, e.getMaxY(), 0.01);

    }

    @Test
    public void testRead() throws IOException {
        Cursor<Feature> c = shp.read(null);
        
        assertNotNull(c);
        for (int i = 0; i < 49; i++) {
            assertTrue(c.hasNext());

            Feature f = c.next();
            assertNotNull(f);

            assertTrue(f.geometry() instanceof MultiPolygon);
            assertNotNull(f.get("STATE_NAME"));
        }

        assertFalse(c.hasNext());
        assertNull(c.next());
        c.close();
    }
}
