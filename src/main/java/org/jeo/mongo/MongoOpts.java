package org.jeo.mongo;

import java.io.IOException;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.ServerAddress;

public class MongoOpts {
    String db; 
    String host = MongoDB.HOST.getDefault();
    Integer port = MongoDB.PORT.getDefault();
    String user = MongoDB.USER.getDefault();
    String passwd;
    
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
    
    public MongoOpts passwd(String passwd) {
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
    
    public String getPasswd() {
        return passwd;
    }

    public DB connect() throws IOException {
        ServerAddress server = new ServerAddress(host, port);
        Mongo mongo = new Mongo(server);
        DB database = mongo.getDB(db);
        database.authenticate(user, passwd != null ? passwd.toCharArray() : new char[]{});

        return database;
    }
}
