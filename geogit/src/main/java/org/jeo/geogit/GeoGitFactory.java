package org.jeo.geogit;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.geogit.api.GeoGIT;
import org.geogit.di.GeogitModule;
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

    static Logger LOGGER = LoggerFactory.getLogger(GeoGitFactory.class);

    @Override
    public GeoGit create(Map<String, Object> map) throws IOException {
        if (map.containsKey(DIR)) {
            File dir = Util.toFile(map.get(DIR));
            if (dir != null) {

                if (!dir.canRead()) {
                    return null;
                }

                Boolean create = map.containsKey(CREATE) ? Util.toBoolean(map.get(CREATE)) : false;

                GeoGIT gg = createGeoGIT(dir, create);
                if (gg != null) {
                    return new GeoGit(gg);
                }
            }

        }
        return null;
    }

    GeoGIT createGeoGIT(File dir, boolean create) {
        if (!dir.exists() && create) {
            dir.mkdirs();
        }

        if (dir.exists() && dir.canRead()) {
            File f = new File(dir, ".geogit");
            if (f.exists() && f.isDirectory()) {
                return newGeoGIT(dir);
            }
            else if (create) {
                GeoGIT gg = newGeoGIT(dir);
                gg.getOrCreateRepository();
                return gg;
            }
        }
        return null;
    }

    GeoGIT newGeoGIT(File dir) {
        //TODO: something about this
        Injector i = Guice.createInjector(
                Modules.override(new GeogitModule()).with(new JEStorageModule()));
        return new GeoGIT(i, dir);
    }
}
