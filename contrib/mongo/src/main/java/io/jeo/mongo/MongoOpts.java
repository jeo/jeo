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
package io.jeo.mongo;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import io.jeo.util.Key;
import io.jeo.util.Password;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.ServerAddress;

import static io.jeo.mongo.MongoDB.*;

public class MongoOpts {
    String db; 
    String host = HOST.def();
    Integer port = PORT.def();
    String user = USER.def();
    Password passwd;

    public static MongoOpts fromMap(Map<?,Object> map) {
        MongoOpts mopts = new MongoOpts(DB.get(map));
        mopts.host(HOST.get(map))
             .port(PORT.get(map))
             .user(USER.get(map))
             .passwd(PASSWD.get(map));
        return mopts;
    }

    public MongoOpts(String db) {
        this.db = db;
    }
    
    public MongoOpts host(String host) {
        this.host = host;
        return this;
    }
    
    public MongoOpts port(Integer port) {
        this.port = port;
        return this;
    }
    
    public MongoOpts user(String user) {
        this.user = user;
        return this;
    }
    
    public MongoOpts passwd(Password passwd) {
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
        Map<Key<?>,Object> map = new LinkedHashMap<Key<?>, Object>();
        map.put(DB, db);
        map.put(HOST, host);
        map.put(PORT, port);
        map.put(USER, user);
        if (passwd != null) {
            map.put(PASSWD, passwd);
        }
        return map;
    }

    public DB connect() throws IOException {
        ServerAddress server = new ServerAddress(host, port);
        Mongo mongo = new Mongo(server);
        DB database = mongo.getDB(db);
        database.authenticate(user, passwd != null ? passwd.get(): new char[]{});

        return database;
    }
}
