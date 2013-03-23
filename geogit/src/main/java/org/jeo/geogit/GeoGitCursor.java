package org.jeo.geogit;

import java.io.IOException;
import java.util.Iterator;

import org.geogit.api.FeatureBuilder;
import org.geogit.api.NodeRef;
import org.geogit.api.RevFeature;
import org.geogit.api.RevFeatureType;
import org.geogit.api.plumbing.RevObjectParse;
import org.jeo.data.Cursor;
import org.jeo.feature.Feature;
import org.jeo.geotools.GT;
import org.opengis.feature.simple.SimpleFeature;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

public class GeoGitCursor extends Cursor<Feature> {

    Iterator<NodeRef> nodeit;
    GeoGitDataset dataset;

    
    RevObjectParse parseOp;
    FeatureBuilder featureBuilder;
    
    GeoGitCursor(Iterator<NodeRef> nodeit, GeoGitDataset dataset) {
        this.nodeit = nodeit;
        this.dataset = dataset;

        featureBuilder = new FeatureBuilder(dataset.featureType());
        parseOp = dataset.getGeoGIT().command(RevObjectParse.class);
    }

    @Override
    public boolean hasNext() throws IOException {
        return nodeit.hasNext();
    }

    @Override
    public Feature next() throws IOException {
        NodeRef ref = nodeit.next();
        parseOp.setObjectId(ref.objectId());

        Optional<RevFeature> f = parseOp.call(RevFeature.class);
        Preconditions.checkState(f.isPresent());

        return GT.feature(
            (SimpleFeature)featureBuilder.build(ref.name(), f.get()), dataset.getSchema());
    }
    
    @Override
    public void close() throws IOException {
    }


}
