package org.jeo.mongo;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.jeo.data.Driver;
import org.jeo.util.Key;

public class MongoDB implements Driver<MongoWorkspace> {

    public static final Key<String> DB = new Key<String>("db", String.class);
    
    public static final Key<String> HOST = new Key<String>("host", String.class, "localhost");
    
    public static final Key<Integer> PORT = new Key<Integer>("port", Integer.class, 27017);
    
    public static final Key<String> USER = 
            new Key<String>("user", String.class, System.getProperty("user.name"));
    
    public static final Key<String> PASSWD = new Key<String>("passwd", String.class);

    public static MongoWorkspace open(MongoOpts opts) throws IOException {
        return new MongoWorkspace(opts);
    }

    @Override
    public String getName() {
        return "MongoDB";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("mongo");
    }

    @Override
    public Class<MongoWorkspace> getType() {
        return MongoWorkspace.class;
    }

    @Override
    public List<Key<?>> getKeys() {
        return (List) Arrays.asList(DB, HOST, PORT, USER, PASSWD);
    }

    @Override
    public boolean canOpen(Map<?, Object> opts) {
        return DB.has(opts);
    }

    @Override
    public MongoWorkspace open(Map<?, Object> opts) throws IOException {
        MongoOpts mopts = new MongoOpts(DB.get(opts));
        mopts.host(HOST.get(opts))
             .port(PORT.get(opts))
             .user(USER.get(opts))
             .passwd(PASSWD.get(opts));

        return new MongoWorkspace(mopts);
    }

}
