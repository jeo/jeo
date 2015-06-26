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

import io.jeo.TestData;
import io.jeo.vector.Feature;
import io.jeo.vector.VectorQuery;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;

import java.net.HttpURLConnection;
import java.net.URL;

public class SolrTests {

    public static String URL = "http://localhost:8983/solr";

    public static void connect() throws Exception  {
        HttpURLConnection conn = (HttpURLConnection) new URL(URL).openConnection();
        conn.connect();
    }

    public static void setupStatesData() throws Exception {
        SolrClient solr = new HttpSolrClient(URL+"/states");
        solr.deleteByQuery("*:*");

        for (Feature f : TestData.states().cursor(new VectorQuery())) {
            SolrInputDocument doc = new SolrInputDocument();

            doc.addField("id", f.id());
            copy("STATE_NAME", f, doc);
            copy("STATE_ABBR", f, doc);
            copy("SAMP_POP", f, doc);
            copy("P_MALE", f, doc);
            copy("P_FEMALE", f, doc);

            doc.addField("geom", f.geometry().toText());
            solr.add(doc);
        }

        solr.commit();
    }

    static void copy(String fld, Feature f, SolrInputDocument doc) {
        doc.addField(fld, f.get(fld));
    }
}
