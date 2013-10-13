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
