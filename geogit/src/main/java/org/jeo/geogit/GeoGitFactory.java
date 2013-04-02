package org.jeo.geogit;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.geogit.api.GeoGIT;
import org.geogit.api.porcelain.ConfigOp;
import org.geogit.api.porcelain.ConfigOp.ConfigAction;
import org.geogit.di.GeogitModule;
import org.geogit.repository.Repository;
import org.geogit.storage.bdbje.JEStorageModule;
import org.jeo.data.WorkspaceFactory;
import org.jeo.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;

public class GeoGitFactory implements WorkspaceFactory<GeoGit> {

    public static final String DIR = "dir";

    public static final String CREATE = "create";

    public static final String USER = "user.name";

    public static final String EMAIL = "user.email";
    
    static Logger LOGGER = LoggerFactory.getLogger(GeoGitFactory.class);

    public GeoGit create(File dir, boolean create, String user, String email) {
        GeoGIT gg = createGeoGIT(dir, create, user, email);
        if (gg != null) {
            return new GeoGit(gg);
        }
        return null;
    }

    @Override
    public GeoGit create(Map<String, Object> map) throws IOException {
        if (map.containsKey(DIR)) {
            File dir = Util.toFile(map.get(DIR));
            if (dir != null) {

                Boolean create = map.containsKey(CREATE) ? Util.toBoolean(map.get(CREATE)) : false;
                String user = map.containsKey(USER) ? map.get(USER).toString() : null;
                String email = map.containsKey(EMAIL) ? map.get(EMAIL).toString() : null;

                return create(dir, create, user, email);
            }

        }
        return null;
    }

    GeoGIT createGeoGIT(File dir, boolean create, String user, String email) {
        if (!dir.exists() && create) {
            dir.mkdirs();
        }

        GeoGIT gg = null;
        if (dir.exists() && dir.canRead()) {
            File f = new File(dir, ".geogit");
            if (f.exists() && f.isDirectory()) {
                gg = newGeoGIT(dir);
            }
            else if (create) {
                gg = newGeoGIT(dir);
                gg.getOrCreateRepository();
            }
        }

        if (gg == null) {
            return null;
        }

        user = user != null ? user : System.getProperty("user.name");
        email = email != null ? email : user + "@localhost";

        Repository repo = gg.getRepository();
        repo.command(ConfigOp.class)
            .setAction(ConfigAction.CONFIG_SET).setName("user.name").setValue(user).call();
        repo.command(ConfigOp.class)
            .setAction(ConfigAction.CONFIG_SET).setName("user.email").setValue(email).call();

        return gg;
    }

    GeoGIT newGeoGIT(File dir) {
        //TODO: something about this
        Injector i = Guice.createInjector(
                Modules.override(new GeogitModule()).with(new JEStorageModule()));
        return new GeoGIT(i, dir);
    }
}
