package org.jeo.postgis;

import java.util.LinkedHashMap;
import java.util.Map;

import org.jeo.util.Key;
import org.jeo.util.Password;

import static org.jeo.postgis.PostGIS.*;

/**
 * Specifies options for connecting to a postgis database.
 * <p>
 * Usage:
 * <pre><code>
 * new MongoOpts("jeo").host("localhost").port(5432).user("bob").passwd("secret");
 * </code></pre>
 * </p>
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class PostGISOpts {

    String db; 
    String host = HOST.getDefault();
    Integer port = PORT.getDefault();
    String user = USER.getDefault();
    Password passwd;

    public static PostGISOpts fromMap(Map<?,Object> map) {
        PostGISOpts pgopts = new PostGISOpts(DB.get(map));

        if (HOST.has(map)) {
            pgopts.host(HOST.get(map));
        }
        if (PORT.has(map)) {
            pgopts.port(PORT.get(map));
        }
        if (USER.has(map)) {
            pgopts.user(USER.get(map));
        }
        if (PASSWD.has(map)) {
            pgopts.passwd(PASSWD.get(map));
        }

        return pgopts;
    }

    public PostGISOpts(String db) {
        this.db = db;
    }

    public PostGISOpts host(String host) {
        this.host = host;
        return this;
    }

    public PostGISOpts port(Integer port) {
        this.port = port;
        return this;
    }

    public PostGISOpts user(String user) {
        this.user = user;
        return this;
    }

    public PostGISOpts passwd(Password passwd) {
        this.passwd = passwd;
        return this;
    }

    public String getDb() {
        return db;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public String getUser() {
        return user;
    }

    public Password getPasswd() {
        return passwd;
    }

    public Map<Key<?>,Object> toMap() {
        Map<Key<?>, Object> map = new LinkedHashMap<Key<?>, Object>();
        map.put(DB, db);
        map.put(HOST, host);
        map.put(PORT, port);
        map.put(USER, user);
        if (passwd != null) {
            map.put(PASSWD, passwd);
        }
        return map;
    }
}
