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
    String schema;
    Password passwd;

    public static PostGISOpts fromMap(Map<?,Object> map) {
        PostGISOpts pgopts = new PostGISOpts(DB.get(map));

        if (SCHEMA.has(map)) {
            pgopts.schema(SCHEMA.get(map));
        }
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

    public PostGISOpts schema(String schema) {
        this.schema = schema;
        return this;
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

    public String db() {
        return db;
    }

    public String schema() {
        return schema;
    }

    public String host() {
        return host;
    }

    public Integer port() {
        return port;
    }

    public String user() {
        return user;
    }

    public Password passwd() {
        return passwd;
    }

    public Map<Key<?>,Object> toMap() {
        Map<Key<?>, Object> map = new LinkedHashMap<Key<?>, Object>();
        map.put(DB, db);
        map.put(HOST, host);
        map.put(PORT, port);
        map.put(USER, user);
        if (schema != null) {
            map.put(SCHEMA, schema);
        }
        if (passwd != null) {
            map.put(PASSWD, passwd);
        }
        return map;
    }
}
