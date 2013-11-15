package org.jeo.ogr;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Shapefile extends OGRDriver<OGRDataset> {

    public static OGRDataset open(File file) throws IOException {
        return new Shapefile().open((Map)Collections.singletonMap(FILE, file));
    }

    @Override
    public String getName() {
        return "Shapefile";
    }

    @Override
    public Class<OGRDataset> getType() {
        return OGRDataset.class;
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("shp");
    }

    @Override
    protected String getOGRDriverName() {
        return "ESRI Shapefile";
    }

    @Override
    protected OGRDataset open(OGRWorkspace workspace) throws IOException {
        return workspace.get(0);
    }
}
