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
package org.jeo.postgis;

import java.sql.Connection;

import org.junit.Assume;
import org.postgresql.ds.PGPoolingDataSource;

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
}
