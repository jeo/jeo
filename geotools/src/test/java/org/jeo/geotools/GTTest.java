package org.jeo.geotools;
import static org.junit.Assert.*;

import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.jeo.feature.Feature;
import org.jeo.feature.Features;
import org.jeo.feature.MapFeature;
import org.jeo.feature.Schema;
import org.jeo.geom.GeometryBuilder;
import org.jeo.proj.Proj;
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
        Schema schema = Features.schema("widgets", "geometry", Point.class, "name", 
            String.class, "id", Integer.class, "price", Double.class); 
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
    }

    @Test
    public void testFromFeature() {
        Feature f = MapFeature.create("geometry", new GeometryBuilder().point(0,0), 
            "id", 1, "name", "bomb", "price", 10.99);
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
