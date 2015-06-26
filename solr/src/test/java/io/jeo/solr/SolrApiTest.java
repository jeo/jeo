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

import io.jeo.Tests;
import io.jeo.vector.VectorApiTestBase;
import io.jeo.vector.VectorDataset;
import org.junit.Assume;
import org.junit.BeforeClass;

public class SolrApiTest extends VectorApiTestBase {

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
    public static void setupData() throws Exception {
        SolrTests.setupStatesData();
//        Tests.debugLogging("io.jeo.solr");
    }

    @Override
    protected VectorDataset createVectorData() throws Exception {
        return Solr.open(SolrTests.URL).get("states");
    }
}
