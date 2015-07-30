package io.jeo.mongo;

import com.mongodb.DB;
import org.junit.Assume;

public class MongoTests {

    public static DB connect() {
        Exception error = null;
        DB db = null;
        try {
            db = new MongoOpts("jeo").connect();
            db.getCollectionNames();
        } catch (Exception e) {
            error = e;
        }

        Assume.assumeNoException(error);
        Assume.assumeNotNull(db);
        return db;
    }
}
