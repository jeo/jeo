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

import java.util.LinkedHashMap;
import java.util.Map;

import io.jeo.util.Key;
import io.jeo.util.Password;

/**
 * Specifies options for connecting to a postgis database.
 * <p>
 * Usage:
 * <pre><code>
 * new PostGISOpts("jeo").host("localhost").port(5432).user("bob").passwd("secret");
 * </code></pre>
 * </p>
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class PostGISOpts {

    String db; 
    String host = PostGIS.HOST.def();
    Integer port = PostGIS.PORT.def();
    String user = PostGIS.USER.def();
    String schema;
    Password passwd;

    public static PostGISOpts fromMap(Map<?,Object> map) {
        PostGISOpts pgopts = new PostGISOpts(PostGIS.DB.get(map));

        if (PostGIS.SCHEMA.in(map)) {
            pgopts.schema(PostGIS.SCHEMA.get(map));
        }
        if (PostGIS.HOST.in(map)) {
            pgopts.host(PostGIS.HOST.get(map));
        }
        if (PostGIS.PORT.in(map)) {
            pgopts.port(PostGIS.PORT.get(map));
        }
        if (PostGIS.USER.in(map)) {
            pgopts.user(PostGIS.USER.get(map));
        }
        if (PostGIS.PASSWD.in(map)) {
            pgopts.passwd(PostGIS.PASSWD.get(map));
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
        map.put(PostGIS.DB, db);
        map.put(PostGIS.HOST, host);
        map.put(PostGIS.PORT, port);
        map.put(PostGIS.USER, user);
        if (schema != null) {
            map.put(PostGIS.SCHEMA, schema);
        }
        if (passwd != null) {
            map.put(PostGIS.PASSWD, passwd);
        }
        return map;
    }
}
