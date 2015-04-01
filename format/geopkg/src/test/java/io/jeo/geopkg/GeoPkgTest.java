/* Copyright 2013 The jeo project. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jeo.geopkg;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import io.jeo.data.Cursor;
import io.jeo.vector.VectorQuery;
import io.jeo.vector.VectorDataset;
import io.jeo.vector.Feature;
import io.jeo.vector.Schema;
import io.jeo.geom.Geom;
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

        Cursor<Feature> c = widgets.cursor(new VectorQuery().append());

        Feature f = c.next();
        f.put(Geom.point(0, 0));
        f.put("name", "zero");

        f = c.write().next();
        f.put(Geom.point(1, 1));
        f.put("name", "one");
        c.write().close();

        assertEquals(2, widgets.count(new VectorQuery()));
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
