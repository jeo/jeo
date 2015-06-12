/* Copyright 2015 The jeo project. All rights reserved.
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
package io.jeo.solr;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.vividsolutions.jts.geom.Envelope;
import io.jeo.data.Dataset;
import io.jeo.data.Handle;
import io.jeo.util.Consumer;
import io.jeo.vector.Feature;
import io.jeo.vector.FeatureCursor;
import io.jeo.vector.Schema;
import io.jeo.vector.VectorQuery;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SolrTest {

    @BeforeClass
    public static void connect()  {
        try {
            SolrTests.connect();
        }
        catch(Exception e) {
            Assume.assumeTrue(false);
        }
    }

    @BeforeClass
    public static void setUpData() throws Exception {
        SolrTests.setupStatesData();
    }

    static SolrWorkspace solr;

    @BeforeClass
    public static void open() throws Exception {
        solr = Solr.open(SolrTests.URL);
    }

    @Test
    public void testList() throws IOException {
        Iterables.find(solr.list(), new Predicate<Handle<Dataset>>() {
            @Override
            public boolean apply(Handle<Dataset> input) {
                return input.name().equals("jeo");
            }
        });
    }

    @Test
    public void testSchema() throws IOException {
        SolrDataset data = solr.get("jeo");
        Schema schema = data.schema();

        assertNotNull(schema);
        assertNotNull(schema.geometry());
        assertEquals(String.class, schema.field("STATE_NAME").type());
        assertEquals(Integer.class, schema.field("SAMP_POP").type());
        assertEquals(Double.class, schema.field("P_MALE").type());
    }

    @Test
    public void testCount() throws IOException {
        SolrDataset data = solr.get("jeo");
        assertEquals(49, data.count(new VectorQuery()));
    }

    @Test
    public void testCursor() throws Exception {
        SolrDataset data = solr.get("jeo");

        Envelope bbox = new Envelope(-106.649513, -93.507217, 25.845198, 36.493877);
        data.cursor(new VectorQuery().bounds(bbox)).each(new Consumer<Feature>() {
            @Override
            public void accept(Feature val) {
                System.out.println(val.get("STATE_NAME"));
            }
        });
    }
}