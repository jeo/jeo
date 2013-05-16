package org.jeo.geojson;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.jeo.data.FileDriver;
import org.jeo.data.VectorDriver;
import org.jeo.feature.Schema;

public class GeoJSON extends FileDriver<GeoJSONDataset> implements VectorDriver<GeoJSONDataset> {

    @Override
    public String getName() {
        return "GeoJSON";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("json");
    }
    
    @Override
    public Class<GeoJSONDataset> getType() {
        return GeoJSONDataset.class;
    }

    @Override
    public boolean canCreate(Map<?, Object> opts) {
        return FILE.has(opts);
    }

    @Override
    public GeoJSONDataset create(Map<?, Object> opts, Schema schema) throws IOException {
        File file = FILE.get(opts);
        return new GeoJSONDataset(file);
    }

    @Override
    public GeoJSONDataset open(File file, Map<?, Object> opts) throws IOException {
        return new GeoJSONDataset(file);
    }
}
