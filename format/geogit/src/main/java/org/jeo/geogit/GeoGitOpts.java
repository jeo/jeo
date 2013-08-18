package org.jeo.geogit;

import static org.jeo.geogit.GeoGit.*;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jeo.util.Key;

public class GeoGitOpts {

    File file;
    
    String user = USER.getDefault();
    String email = EMAIL.getDefault();
    boolean emailIsSet = false;

    public static GeoGitOpts fromMap(Map<?,Object> map) {
        GeoGitOpts ggopts = new GeoGitOpts(FILE.get(map));

        if (USER.has(map)) {
            ggopts.user(USER.get(map));
        }
        if (EMAIL.has(map)) {
            ggopts.email(EMAIL.get(map));
        }

        return ggopts;
    }

    public GeoGitOpts(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }
    
    public GeoGitOpts user(String user) {
        this.user = user;
        if (!emailIsSet) {
            email = user + "@localhost";
        }
        return this;
    }

    public String getUser() {
        return user;
    }

    public GeoGitOpts email(String email) {
        this.email = email;
        this.emailIsSet = true;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public Map<Key<?>,Object> toMap() {
        LinkedHashMap<Key<?>, Object> map = new LinkedHashMap<Key<?>, Object>();
        map.put(FILE, file);
        map.put(USER, user);
        map.put(EMAIL, email);

        return map;
    }
}
