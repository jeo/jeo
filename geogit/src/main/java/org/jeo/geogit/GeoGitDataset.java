package org.jeo.geogit;

import java.io.IOException;

import org.geogit.api.Bounded;
import org.geogit.api.GeoGIT;
import org.geogit.api.NodeRef;
import org.geogit.api.RevTree;
import org.geogit.api.plumbing.LsTreeOp;
import org.geogit.api.plumbing.LsTreeOp.Strategy;
import org.geogit.api.plumbing.RevObjectParse;
import org.jeo.data.Cursor;
import org.jeo.data.Vector;
import org.jeo.feature.Feature;
import org.jeo.feature.Schema;
import org.opengis.feature.simple.SimpleFeatureType;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.vividsolutions.jts.geom.Envelope;

public class GeoGitDataset implements Vector {

    GeoGit geogit;
    Schema schema;

    public GeoGitDataset(Schema schema, GeoGit geogit) {
        this.schema = schema;
        this.geogit = geogit;
    }

    public GeoGIT getGeoGIT() {
        return geogit.getGeoGIT();
    }
    @Override
    public String getName() {
        return schema.getName();
    }

    @Override
    public String getTitle() {
        return getName();
    }
    
    @Override
    public String getDescription() {
        return getName();
    }

    @Override
    public Schema getSchema() throws IOException {
        return schema;
    }

    @Override
    public CoordinateReferenceSystem getCRS() {
        return schema.crs();
    }

    @Override
    public long count(Envelope bbox) throws IOException {
        if (bbox == null) {
            return countAll();
        }
        return -1;
    }

    long countAll() {
        return tree().size();
    }

    @Override
    public Envelope bounds() throws IOException {
        Envelope bounds = new Envelope();
        ref().expand(bounds);
        return bounds;
    }

    @Override
    public Cursor<Feature> read(final Envelope bbox) throws IOException {
        LsTreeOp ls = geogit.getGeoGIT().command(LsTreeOp.class)
            .setStrategy(Strategy.FEATURES_ONLY).setReference(ref().path());

        if (bbox != null && !bbox.isNull()) {
            ls.setBoundsFilter(new Predicate<Bounded>() {
                @Override
                public boolean apply(Bounded input) {
                    return input.intersects(bbox);
                }
            });
        }

        return new GeoGitCursor(ls.call(), this);
    }

    @Override
    public void add(Feature f) throws IOException {
        // TODO Auto-generated method stub
    
    }

    NodeRef ref() {
        return geogit.typeRef(getName());
    }

    RevTree tree() {
        String refspec = geogit.rootRef() + ":" + ref().path();
        Optional<RevTree> tree = 
           geogit.getGeoGIT().command(RevObjectParse.class).setRefSpec(refspec).call(RevTree.class);
        return tree.get();
    }

    SimpleFeatureType featureType() {
        return geogit.featureType(ref());
    }
}
