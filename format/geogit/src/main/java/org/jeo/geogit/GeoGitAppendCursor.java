package org.jeo.geogit;

import java.io.IOException;

import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.jeo.data.Cursor;
import org.jeo.feature.Feature;
import org.jeo.geotools.GT;
import org.jeo.geotools.GTFeature;

public class GeoGitAppendCursor extends Cursor<Feature> {

    GeoGitDataset dataset;
    GeoGitTransaction tx;

    GTFeature curr;
    SimpleFeatureBuilder featureBuilder;

    GeoGitAppendCursor(GeoGitDataset dataset, GeoGitTransaction tx) {
        super(Mode.APPEND);
        this.dataset = dataset;
        this.tx = tx;

        featureBuilder = new SimpleFeatureBuilder(dataset.featureType());
    }

    @Override
    public boolean hasNext() throws IOException {
        return true;
    }

    @Override
    public Feature next() throws IOException {
        curr = GT.feature(featureBuilder.buildFeature(null), dataset.getSchema());
        return curr;
    }

    @Override
    protected void doWrite() throws IOException {
        dataset.insert(curr.getFeature(), tx);
    }

    @Override
    public void close() throws IOException {

    }
}
