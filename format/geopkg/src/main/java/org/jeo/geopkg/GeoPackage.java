package org.jeo.geopkg;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jeo.data.FileDriver;
import org.jeo.util.Key;

public class GeoPackage extends FileDriver<GeoPkgWorkspace> {

    /**
     * User key, defaults to no user.
     */
    public static final Key<String> USER = new Key<String>("user", String.class);

    /**
     * Password key. 
     */
    public static final Key<String> PASSWD = new Key<String>("passwd", String.class);

    public static GeoPkgWorkspace open(File file) throws IOException {
        return new GeoPackage().open(file, Collections.emptyMap());
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
        return Arrays.asList("geopkg");
    }

    @Override
    public Class<GeoPkgWorkspace> getType() {
        return GeoPkgWorkspace.class;
    }

    @Override
    public GeoPkgWorkspace open(File file, Map<?, Object> opts) throws IOException {
        return new GeoPkgWorkspace(
            new GeoPkgOpts(file).user(USER.get(opts)).passwd(PASSWD.get(opts)));
    }

}
