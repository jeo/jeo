package org.jeo.geogit;

import java.io.File;

public class GeoGitOpts {

    File file;
    boolean create = GeoGit.CREATE.getDefault();
    String user = GeoGit.USER.getDefault();

    String email = GeoGit.EMAIL.getDefault();
    boolean emailIsSet = false;

    public GeoGitOpts(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }
    
    public GeoGitOpts create(boolean create) {
        this.create = create;
        return this;
    }

    public boolean isCreate() {
        return create;
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
}
