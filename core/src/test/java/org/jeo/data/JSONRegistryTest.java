package org.jeo.data;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.jeo.data.mem.MemWorkspace;
import org.jeo.data.mem.Memory;
import org.jeo.json.JSONObject;
import org.jeo.json.JSONValue;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class JSONRegistryTest {

    JSONRegistry reg;

    @Before
    public void setUp() throws Exception {
        String json = "{" + 
          "\"foo\": {" +
              "\"driver\": \"mem\"" + 
          "}" + 
        "}";

        reg = new JSONRegistry((JSONObject) JSONValue.parseWithException(json));
    }

    @Test
    public void testList() throws Exception {
        Iterables.find(reg.list(), new Predicate<DataRef>() {
            @Override
            public boolean apply(DataRef ref) {
                return "foo".equals(ref.getName()) 
                    && Memory.class.isAssignableFrom(ref.getDriver().getClass());
            }
        });
    }

    @Test
    public void testGet() throws Exception {
        Workspace obj = (Workspace) reg.get("foo");
        assertNotNull(obj);

        assertTrue(obj instanceof MemWorkspace);
    }
}
