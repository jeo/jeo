package org.jeo.data;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.jeo.data.mem.MemWorkspace;
import org.jeo.data.mem.Memory;
import org.jeo.filter.Filters;
import org.jeo.json.JSONObject;
import org.jeo.json.JSONValue;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

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
        Iterables.find(repo.query(Filters.all()), new Predicate<Handle<Object>>() {
            @Override
            public boolean apply(Handle<Object> h) {
                return "foo".equals(h.getName()) 
                    && Memory.class.isAssignableFrom(h.getDriver().getClass());
            }
        });
    }

    @Test
    public void testGet() throws Exception {
        Workspace obj = (Workspace) repo.get("foo");
        assertNotNull(obj);

        assertTrue(obj instanceof MemWorkspace);
    }
}
