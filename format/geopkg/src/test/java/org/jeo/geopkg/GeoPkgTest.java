package org.jeo.geopkg;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.jeo.data.Cursor;
import org.jeo.data.Query;
import org.jeo.data.VectorDataset;
import org.jeo.feature.Feature;
import org.jeo.feature.Schema;
import org.jeo.geom.Geom;
import org.junit.Before;
import org.junit.Test;

import com.vividsolutions.jts.geom.Point;

public class GeoPkgTest extends GeoPkgTestSupport {

    @Before
    public void setUp() throws ClassNotFoundException {
        Class.forName(GeoPkgWorkspace.class.getCanonicalName());
    }

    @Test
    public void testOpen() throws Exception {
        assertNotNull(GeoPackage.open(newFile("foo.gpkg")));
    }

    @Test
    public void testCreate() throws Exception {
        Schema schema = Schema.build("widgets").field("geometry", Point.class, "epsg:4326")
            .field("name", String.class).schema();

        GeoPkgWorkspace gpkg = GeoPackage.open(newFile("stuff.gpkg"));
        assertNotNull(gpkg);

        VectorDataset widgets = gpkg.create(schema);
        assertNotNull(widgets);

        Cursor<Feature> c = widgets.cursor(new Query().append());

        Feature f = c.next();
        f.put(Geom.point(0, 0));
        f.put("name", "zero");

        f = c.write().next();
        f.put(Geom.point(1, 1));
        f.put("name", "one");
        c.write().close();

        assertEquals(2, widgets.count(new Query()));
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
