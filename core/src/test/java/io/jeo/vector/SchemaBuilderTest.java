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
package io.jeo.vector;

import static org.junit.Assert.*;

import io.jeo.proj.Proj;
import org.junit.Test;

import com.vividsolutions.jts.geom.Point;
import java.util.Arrays;

public class SchemaBuilderTest {

    @Test
    public void testFieldSpec() {
        Schema schema = new SchemaBuilder("widgets")
            .fields("sp:String,ip:Integer,pp:Point:srid=4326").schema();
        assertEquals(3, schema.fields().size());

        assertEquals(String.class, schema.field("sp").type());
        assertEquals(Integer.class, schema.field("ip").type());
        assertEquals(Point.class, schema.field("pp").type());
        assertEquals(Proj.EPSG_4326, schema.field("pp").crs());
    }

    @Test
    public void testSelectFields() {
        Schema schema = new SchemaBuilder("widgets")
            .fields("sp:String,ip:Integer,pp:Point:srid=4326").schema();

        Schema selected = SchemaBuilder.select(schema, Arrays.asList("sp","pp","blah"));
        assertEquals(2, selected.fields().size());
        assertEquals(schema.field("sp"), selected.field("sp"));
        assertEquals(schema.field("pp"), selected.field("pp"));
        // ordering
        assertEquals(schema.field("sp"), selected.fields.get(0));
        assertEquals(schema.field("pp"), selected.fields.get(1));
    }
}
