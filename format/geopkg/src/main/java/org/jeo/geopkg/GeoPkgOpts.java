package org.jeo.geopkg;

import static org.jeo.geopkg.GeoPackage.*;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jeo.util.Key;
import org.jeo.util.Password;

public class GeoPkgOpts {

    File file;
    String user;
    Password passwd;

    public static GeoPkgOpts fromMap(Map<?,Object> map) {
        return new GeoPkgOpts(FILE.get(map)).user(USER.get(map)).passwd(PASSWD.get(map));
    }

    public GeoPkgOpts(File file) {
        this.file = file;
    }

    public GeoPkgOpts user(String user) {
        this.user = user;
        return this;
    }

    public GeoPkgOpts passwd(Password passwd) {
        this.passwd = passwd;
        return this;
    }

    public File getFile() {
        return file;
    }

    public String getUser() {
        return user;
    }

    public Password getPasswd() {
        return passwd;
    }

    public Map<Key<?>,Object> toMap() {
        Map<Key<?>,Object> map = new LinkedHashMap<Key<?>, Object>();
        map.put(FILE, file);
        if (user != null) {
            map.put(USER, user);
        }
        if (passwd != null) {
            map.put(PASSWD, passwd);
        }
        return map;
    }
}
