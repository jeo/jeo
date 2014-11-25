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

import static org.junit.Assert.*;

import org.jeo.data.mem.MemWorkspace;
import org.jeo.data.mem.Memory;
import org.jeo.json.JSONObject;
import org.jeo.json.JSONValue;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import org.jeo.filter.Filters;

public class JSONRegistryTest {

    JSONRepository repo;

    @Before
    public void setUp() throws Exception {
        String json = "{" + 
          "\"foo\": {" +
              "\"driver\": \"mem\"" + 
          "}" + 
        "}";

        repo = new JSONRepository((JSONObject) JSONValue.parseWithException(json));
    }

    @Test
    public void testList() throws Exception {
        Iterator<Handle<?>> list = repo.query(Filters.all()).iterator();
        assertTrue(list.hasNext());
        Handle<?> next = list.next();
        assertEquals("foo", next.getName());
        assertTrue(Memory.class.isAssignableFrom(next.getDriver().getClass()));
    }

    @Test
    public void testGet() throws Exception {
        Workspace obj = (Workspace) repo.get("foo", Workspace.class);
        assertNotNull(obj);

        assertTrue(obj instanceof MemWorkspace);
    }
}
