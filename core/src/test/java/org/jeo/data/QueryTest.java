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
package org.jeo.data;

import java.util.List;
import org.jeo.feature.Schema;
import org.jeo.feature.SchemaBuilder;
import org.junit.Test;
import static org.junit.Assert.*;

public class QueryTest {

    @Test
    public void testGetOrderedFields() {
        Schema schema = new SchemaBuilder("widgets")
            .fields("sp:String,ip:Integer,pp:Point:srid=4326").schema();
        List<String> fields = new Query().fields("pp","ip","blah").getFields(schema);
        assertEquals(2, fields.size());
        assertEquals("ip", fields.get(0));
        assertEquals("pp", fields.get(1));
    }

}
