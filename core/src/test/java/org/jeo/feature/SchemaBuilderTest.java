package org.jeo.feature;

import static org.junit.Assert.*;

import org.jeo.proj.Proj;
import org.junit.Test;

import com.vividsolutions.jts.geom.Point;

public class SchemaBuilderTest {

    @Test
    public void testFieldSpec() {
        Schema schema = new SchemaBuilder("widgets")
            .fields("sp:String,ip:Integer,pp:Point:srid=4326").schema();
        assertEquals(3, schema.getFields().size());

        assertEquals(String.class, schema.field("sp").getType());
        assertEquals(Integer.class, schema.field("ip").getType());
        assertEquals(Point.class, schema.field("pp").getType());
        assertEquals(Proj.EPSG_4326, schema.field("pp").getCRS());
    }
}
