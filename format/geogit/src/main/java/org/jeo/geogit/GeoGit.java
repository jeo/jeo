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

    /**
     * Key to create repository if it doesn't exist, defaults to <tt>false</tt>.
     */
    public static final Key<Boolean> CREATE = new Key<Boolean>("create", Boolean.class, true);

    /**
     * Username key, defaults to <tt>System.getProperty("user.name")</tt>
     */
    public static final Key<String> USER = 
        new Key<String>("user.name", String.class, System.getProperty("user.name"));

    /**
     * Email key, defaults to <tt>System.getProperty("user.name") + "@localhost"</tt>
     */
    public static final Key<String> EMAIL = 
        new Key<String>("user.email", String.class, USER.getDefault() + "@localhost");

    public static GeoGitWorkspace open(GeoGitOpts opts) throws IOException {
        return new GeoGitWorkspace(newGeoGIT(opts), opts);
    }

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
        GeoGitOpts ggopts = new GeoGitOpts(file);
        if (CREATE.has(opts)) {
            ggopts.create(CREATE.get(opts));
        }
        if (USER.has(opts)) {
            ggopts.user(USER.get(opts));
        }
        if (EMAIL.has(opts)) {
            ggopts.email(EMAIL.get(opts));
        }

        return new GeoGitWorkspace(newGeoGIT(ggopts), ggopts);
    }

    static GeoGIT newGeoGIT(GeoGitOpts opts) throws IOException {
        File file = opts.getFile();
        if (!file.exists() && opts.isCreate()) {
            if (!file.mkdirs()) {
                throw new IOException("Unable to create directory: " + file.getPath());
            }
        }

        //TODO: something about this
        Injector i = Guice.createInjector(
            Modules.override(new GeogitModule()).with(new JEStorageModule()));

        GeoGIT gg = new GeoGIT(i, file);
        gg.getOrCreateRepository();

        Repository repo = gg.getRepository();
        repo.command(ConfigOp.class).setAction(ConfigAction.CONFIG_SET)
            .setName("user.name").setValue(opts.getUser()).call();
        repo.command(ConfigOp.class).setAction(ConfigAction.CONFIG_SET)
            .setName("user.email").setValue(opts.getEmail()).call();
        
        return gg;
    }
}
