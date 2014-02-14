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
