package org.jeo.geojson;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.jeo.data.FileVectorDriver;
import org.jeo.feature.Schema;

/**
 * GeoJSON format driver.
 * <p>
 * Usage:
 * <pre><code>
 * GeoJSON.open(new File("states.json"));
 * </code></pre>
 * </p>
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class GeoJSON extends FileVectorDriver<GeoJSONDataset> {

    /**
     * Opens a file containing encoded GeoJSON.
     */
    public static GeoJSONDataset open(File file) {
        return new GeoJSONDataset(file);
    }

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
    public GeoJSONDataset open(File file, Map<?, Object> opts) throws IOException {
        return new GeoJSONDataset(file);
    }

    @Override
    protected GeoJSONDataset create(File file, Map<?, Object> opts, Schema schema) 
        throws IOException {
        return new GeoJSONDataset(file);
    }
}
