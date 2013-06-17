package org.jeo.postgis;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

import org.jeo.sql.DbOP;
import org.jeo.util.Pair;
import org.jeo.util.Version;

public class PostGISInfo {

    Version postgis, postgres;
    
    public PostGISInfo(PostGISWorkspace ws) throws IOException {
        Pair<Version,Version> p = ws.run(new DbOP<Pair<Version,Version>>() {
            @Override
            protected Pair<Version, Version> doRun(Connection cx) throws Exception {
                ResultSet rs = 
                    open(open(cx.createStatement()).executeQuery("SELECT postgis_lib_version()"));
                rs.next();
                Version postgis = new Version(rs.getString(1));

                DatabaseMetaData md = cx.getMetaData();
                Version postgres = new Version(String.format(
                    "%s.%s", md.getDatabaseMajorVersion(), md.getDatabaseMinorVersion()));
                return new Pair<Version,Version>(postgis, postgres);
            }
        });

        postgis = p.first();
        postgres = p.second();
    }

    public boolean hasGeography() {
        return postgis.compareTo(new Version("1.5.0")) >= 1;
    }

    public boolean isAtLeastVersion2() {
        return postgis.compareTo(new Version("2.0")) >= 1;
    }
}
