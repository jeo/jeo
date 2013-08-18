package org.jeo.mongo;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.jeo.data.VectorDriver;
import org.jeo.feature.Schema;
import org.jeo.util.Key;
import org.jeo.util.Messages;
import org.jeo.util.Password;

public class MongoDB implements VectorDriver<MongoWorkspace> {

    public static final Key<String> DB = new Key<String>("db", String.class);
    
    public static final Key<String> HOST = new Key<String>("host", String.class, "localhost");
    
    public static final Key<Integer> PORT = new Key<Integer>("port", Integer.class, 27017);
    
    public static final Key<String> USER = 
            new Key<String>("user", String.class, System.getProperty("user.name"));
    
    public static final Key<Password> PASSWD = new Key<Password>("passwd", Password.class);

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
    public boolean canOpen(Map<?, Object> opts, Messages msgs) {
        if (!DB.has(opts)) {
            Messages.of(msgs).report("No " + DB + " option specified");
            return false;
        }

        return true;
    }

    @Override
    public MongoWorkspace open(Map<?, Object> opts) throws IOException {
        return new MongoWorkspace(MongoOpts.fromMap(opts));
    }

    @Override
    public boolean canCreate(Map<?, Object> opts, Messages msgs) {
        Messages.of(msgs).report("Creation not suported");
        return false;
    }

    @Override
    public MongoWorkspace create(Map<?, Object> opts, Schema schema) throws IOException {
        throw new UnsupportedOperationException();
    }
}
