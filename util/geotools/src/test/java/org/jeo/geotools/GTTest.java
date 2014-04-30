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
package org.jeo.geotools;
import static org.junit.Assert.*;

import java.util.Map;

import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.GeometryBuilder;
import org.jeo.feature.BasicFeature;
import org.jeo.feature.Feature;
import org.jeo.feature.Schema;
import org.jeo.feature.SchemaBuilder;
import org.jeo.proj.Proj;
import org.jeo.util.Util;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;

import com.vividsolutions.jts.geom.Point;


public class GTTest {

    @Test
    public void testToSchema() {
        Schema schema = GT.schema(buildFeatureType());
        assertNotNull(schema);

        assertEquals("widgets", schema.getName());
        assertEquals(Proj.crs(4326).getName(), schema.crs().getName());
        assertEquals(4, schema.getFields().size());

        assertNotNull(schema.field("geometry"));
        assertEquals(Point.class, schema.field("geometry").getType());
        assertNotNull(schema.field("name"));
        assertEquals(String.class, schema.field("name").getType());
        assertNotNull(schema.field("id"));
        assertEquals(Integer.class, schema.field("id").getType());
        assertNotNull(schema.field("price"));
        assertEquals(Double.class, schema.field("price").getType());
    }

    @Test
    public void testFromSchema() {
        Schema schema = new SchemaBuilder("widgets").field("geometry", Point.class)
            .field("name", String.class).field("id", Integer.class).field("price", Double.class)
            .schema();
         
        SimpleFeatureType featureType = GT.featureType(schema);

        assertNotNull(featureType.getDescriptor("geometry"));
        assertTrue(featureType.getDescriptor("geometry") instanceof GeometryDescriptor);
        assertEquals(Point.class, featureType.getDescriptor("geometry").getType().getBinding());

        assertNotNull(featureType.getDescriptor("name"));
        assertEquals(String.class, featureType.getDescriptor("name").getType().getBinding());
        
        assertNotNull(featureType.getDescriptor("id"));
        assertEquals(Integer.class, featureType.getDescriptor("id").getType().getBinding());
        
        assertNotNull(featureType.getDescriptor("price"));
        assertEquals(Double.class, featureType.getDescriptor("price").getType().getBinding());

        assertNotNull(featureType.getGeometryDescriptor());
    }

    @Test
    public void testToFeature() {
        SimpleFeatureBuilder b = new SimpleFeatureBuilder(buildFeatureType());
        b.add(new GeometryBuilder().point(0,0));
        b.add("bomb");
        b.add(1);
        b.add(10.99);

        SimpleFeature sf = b.buildFeature(null);
        Feature f = GT.feature(sf);

        assertNotNull(f.get("geometry"));
        assertTrue(f.get("geometry") instanceof Point);
        assertEquals("bomb", f.get("name"));
        assertEquals(1, f.get("id"));
        assertEquals(10.99, f.get("price"));
        assertTrue(f.has("geometry"));
        assertTrue(f.has("price"));
        assertFalse(f.has("NOT THERE AT ALL"));
    }

    @Test
    public void testFromFeature() {
        Feature f = new BasicFeature(null, (Map)Util.map("geometry", new GeometryBuilder().point(0,0), 
            "id", 1, "name", "bomb", "price", 10.99));
        SimpleFeature sf = GT.feature(f);

        assertNotNull(sf.getAttribute("geometry"));
        assertTrue(sf.getAttribute("geometry") instanceof Point);
        assertEquals("bomb", sf.getAttribute("name"));
        assertEquals(1, sf.getAttribute("id"));
        assertEquals(10.99, sf.getAttribute("price"));
    }

    SimpleFeatureType buildFeatureType() {
        SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
        tb.setName("widgets");
        tb.setSRS("EPSG:4326");
        tb.add("geometry", Point.class);
        tb.add("name", String.class);
        tb.add("id", Integer.class);
        tb.add("price", Double.class);
        return tb.buildFeatureType();
    }
}
