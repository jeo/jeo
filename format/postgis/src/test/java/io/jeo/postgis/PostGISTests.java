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
package io.jeo.postgis;

import java.sql.Connection;
import java.sql.Statement;

import io.jeo.TestData;
import io.jeo.vector.Feature;
import io.jeo.vector.VectorQuery;
import org.junit.Assume;
import org.postgresql.ds.PGPoolingDataSource;

import static java.lang.String.format;

public class PostGISTests {

    /*
     * change these params to run against a different server/database
     */
    public static PostGISOpts OPTS = new PostGISOpts("jeo");

    public static void logging() {
        java.util.logging.Logger log = java.util.logging.Logger.getLogger(
            org.slf4j.LoggerFactory.getLogger(PostGIS.class).getName());
        log.setLevel(java.util.logging.Level.FINE);
    
        java.util.logging.ConsoleHandler h = new java.util.logging.ConsoleHandler();
        h.setLevel(java.util.logging.Level.FINE);
        log.addHandler(h);
    }

    public static void connect() throws Exception  {
        PGPoolingDataSource ds = PostGISWorkspace.createDataSource(OPTS); 
        Connection cx = ds.getConnection();
        Assume.assumeNotNull(cx);
        cx.close();
        ds.close();
    }

    public static void setupStatesData() throws Exception {
        PGPoolingDataSource ds = PostGISWorkspace.createDataSource(PostGISTests.OPTS);
        try (Connection cx = ds.getConnection()) {
            try (Statement st = cx.createStatement()) {
                st.executeUpdate("DROP TABLE IF EXISTS states");
                st.executeUpdate(
                    "CREATE TABLE states(id serial PRIMARY KEY, \"STATE_NAME\" VARCHAR, \"STATE_ABBR\" VARCHAR, " +
                     "\"SAMP_POP\" NUMERIC, \"P_MALE\" NUMERIC, \"P_FEMALE\" NUMERIC, geom Geometry(MultiPolygon,4326))"
                );
                for (Feature f : TestData.states().read(new VectorQuery())) {
                    String sql = format(
                        "INSERT INTO states (\"STATE_NAME\", \"STATE_ABBR\", \"SAMP_POP\", \"P_MALE\", " +
                            "\"P_FEMALE\", geom) VALUES ('%s', '%s', %d, %f, %f, ST_GeomFromText('%s',4326))",
                        f.get("STATE_NAME"), f.get("STATE_ABBR"), f.get("SAMP_POP"), f.get("P_MALE"), f.get("P_FEMALE"),
                        f.geometry().toText());
                    st.addBatch(sql);
                }

                st.executeBatch();
            }
        }
        ds.close();
    }
}
