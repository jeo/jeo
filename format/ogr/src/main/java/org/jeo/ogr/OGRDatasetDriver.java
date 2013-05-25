package org.jeo.ogr;

import java.io.IOException;
import java.util.Map;

import org.gdal.ogr.DataSource;

public class OGRDatasetDriver extends OGRDriver<OGRDataset> {

    public OGRDatasetDriver(String name) {
        super(name);
    }

    @Override
    public Class<OGRDataset> getType() {
        return OGRDataset.class;
    }

    @Override
    public OGRDataset open(Map<?, Object> opts) throws IOException {
        try {
            probe();
        } catch (Exception e) {
            throw new IOException(e);
        }

        String path = PATH.get(opts);
        DataSource data = driver().Open(path);

        return new OGRDataset(data, this);
    }
}
