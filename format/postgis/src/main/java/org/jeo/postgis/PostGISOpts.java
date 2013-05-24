package org.jeo.postgis;

public class PostGISOpts {

    String db; 
    String host = PostGIS.HOST.getDefault();
    Integer port = PostGIS.PORT.getDefault();
    String user = PostGIS.USER.getDefault();
    String passwd;

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

    public PostGISOpts passwd(String passwd) {
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
}
