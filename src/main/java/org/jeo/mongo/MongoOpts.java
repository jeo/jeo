package org.jeo.mongo;

import static org.jeo.mongo.MongoDB.*;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jeo.util.Key;
import org.jeo.util.Password;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.ServerAddress;

public class MongoOpts {
    String db; 
    String host = HOST.getDefault();
    Integer port = PORT.getDefault();
    String user = USER.getDefault();
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
