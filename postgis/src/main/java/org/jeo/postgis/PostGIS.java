package org.jeo.postgis;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.jeo.data.Driver;
import org.jeo.util.Key;

public class PostGIS implements Driver<PostGISWorkspace>{

    public static final Key<String> DB = new Key<String>("db", String.class);

    public static final Key<String> HOST = new Key<String>("host", String.class, "localhost");

    public static final Key<Integer> PORT = new Key<Integer>("port", Integer.class, 5432);

    public static final Key<String> USER = 
            new Key<String>("user", String.class, System.getProperty("user.name"));

    public static final Key<String> PASSWD = new Key<String>("passwd", String.class);

    @Override
    public String getName() {
        return "PostGIS";
    }

    @Override
    public Class<PostGISWorkspace> getType() {
        return PostGISWorkspace.class;
    }

    @Override
    public List<Key<? extends Object>> getKeys() {
        return (List) Arrays.asList(DB, HOST, PORT, USER, PASSWD);
    }

    @Override
    public boolean canOpen(Map<?, Object> opts) {
        return DB.has(opts);
    }

    @Override
    public PostGISWorkspace open(Map<?, Object> opts) throws IOException {
        PostGISOpts pgopts = new PostGISOpts(DB.get(opts));
        pgopts.host(HOST.get(opts))
          .port(PORT.get(opts))
          .user(USER.get(opts))
          .passwd(PASSWD.get(opts));

        return new PostGISWorkspace(pgopts);
    }

}
