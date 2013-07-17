package org.jeo.geopkg;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jeo.data.FileDriver;
import org.jeo.util.Key;
import org.jeo.util.Password;

public class GeoPackage extends FileDriver<GeoPkgWorkspace> {

    /**
     * User key, defaults to no user.
     */
    public static final Key<String> USER = new Key<String>("user", String.class);

    /**
     * Password key. 
     */
    public static final Key<Password> PASSWD = new Key<Password>("passwd", Password.class);

    public static GeoPkgWorkspace open(File file) throws IOException {
        return new GeoPackage().open(file, (Map) Collections.singletonMap(FILE, file));
    }

    @Override
    public List<Key<?>> getKeys() {
        return (List) Arrays.asList(FILE, USER, PASSWD);
    }

    @Override
    public String getName() {
        return "GeoPackage";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("gpkg", "geopkg");
    }

    @Override
    public Class<GeoPkgWorkspace> getType() {
        return GeoPkgWorkspace.class;
    }

    @Override
    public GeoPkgWorkspace open(File file, Map<?, Object> opts) throws IOException {
        return new GeoPkgWorkspace(GeoPkgOpts.fromMap(opts));
    }

}
