package org.jeo.geopkg;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.jeo.data.FileDriver;

public class GeoPackage extends FileDriver<GeoPkgWorkspace> {

    public static GeoPkgWorkspace open(File file) throws IOException {
        return new GeoPackage().open(file, null);
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
        return new GeoPkgWorkspace(file);
    }

}
