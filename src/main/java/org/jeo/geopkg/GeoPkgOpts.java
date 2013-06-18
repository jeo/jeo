package org.jeo.geopkg;

import java.io.File;

public class GeoPkgOpts {

    File file;
    String user;
    String passwd;

    public GeoPkgOpts(File file) {
        this.file = file;
    }

    public GeoPkgOpts user(String user) {
        this.user = user;
        return this;
    }

    public GeoPkgOpts passwd(String passwd) {
        this.passwd = passwd;
        return this;
    }

    public File getFile() {
        return file;
    }

    public String getUser() {
        return user;
    }

    public String getPasswd() {
        return passwd;
    }

}
