package org.jeo.geogit;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.geogit.api.GeoGIT;
import org.geogit.api.porcelain.ConfigOp;
import org.geogit.api.porcelain.ConfigOp.ConfigAction;
import org.geogit.di.GeogitModule;
import org.geogit.repository.Repository;
import org.geogit.storage.bdbje.JEStorageModule;
import org.jeo.data.FileDriver;
import org.jeo.util.Key;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;

public class GeoGit extends FileDriver<GeoGitWorkspace> {

    public static final Key<Boolean> CREATE = new Key<Boolean>("create", Boolean.class, true);

    public static final Key<String> USER = 
        new Key<String>("user.name", String.class, System.getProperty("user.name"));

    public static final Key<String> EMAIL = 
        new Key<String>("user.email", String.class, USER.getDefault() + "@localhost");

    @Override
    public String getName() {
        return "GeoGIT";
    }

    @Override
    public List<Key<?>> getKeys() {
        return (List) Arrays.asList(FILE, CREATE, USER, EMAIL);
    }

    @Override
    public boolean canOpen(File file, Map<?,Object> opts) {
        boolean create = CREATE.get(opts);
        if (!create) {
            return super.canOpen(file, opts) && file.isDirectory();
        }
        return true;
    }

    @Override
    public Class<GeoGitWorkspace> getType() {
        return GeoGitWorkspace.class;
    }

    @Override
    public GeoGitWorkspace open(File file, Map<?, Object> opts) throws IOException {
        if (!file.exists() && CREATE.get(opts)) {
            if (!file.mkdirs()) {
                throw new IOException("Unable to create directory: " + file.getPath());
            }
        }

        GeoGIT gg = newGeoGIT(file);
        gg.getOrCreateRepository();

        Repository repo = gg.getRepository();
        repo.command(ConfigOp.class).setAction(ConfigAction.CONFIG_SET)
            .setName("user.name").setValue(USER.get(opts)).call();
        repo.command(ConfigOp.class).setAction(ConfigAction.CONFIG_SET)
            .setName("user.email").setValue(EMAIL.get(opts)).call();

        return new GeoGitWorkspace(gg);
    }

    GeoGIT newGeoGIT(File file) {
        //TODO: something about this
        Injector i = Guice.createInjector(
            Modules.override(new GeogitModule()).with(new JEStorageModule()));
        return new GeoGIT(i, file);
    }
}
