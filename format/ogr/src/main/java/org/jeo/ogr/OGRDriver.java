package org.jeo.ogr;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.gdal.ogr.ogr;
import org.jeo.data.Driver;
import org.jeo.util.Key;

public abstract class OGRDriver<T> implements Driver<T> {

    /**
     * Key specifying the file path.
     */
    public static final Key<String> PATH = new Key<String>("path", String.class);

    String name;

    public OGRDriver(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<String> getAliases() {
        return Collections.emptyList();
    }

    @Override
    public List<Key<?>> getKeys() {
        return (List) Arrays.asList(PATH);
    }

    @Override
    public void probe() throws Exception {
        if (ogr.GetDriverCount() == 0) {
            ogr.RegisterAll();
        }
        if (driver() == null) {
            throw new IllegalStateException("Driver " + name + " not available");
        }
    }

    @Override
    public boolean canOpen(Map<?, Object> opts) {
        return PATH.has(opts);
    }

    protected org.gdal.ogr.Driver driver() {
        return ogr.GetDriverByName(name);
    }
}
