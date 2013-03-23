package org.jeo.geogit;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.geogit.api.GeoGIT;
import org.geogit.api.NodeRef;
import org.geogit.api.Ref;
import org.geogit.api.RevFeatureType;
import org.geogit.api.SymRef;
import org.geogit.api.data.FindFeatureTypeTrees;
import org.geogit.api.plumbing.RefParse;
import org.geogit.api.plumbing.RevObjectParse;
import org.geogit.repository.Repository;
import org.jeo.data.Vector;
import org.jeo.data.Workspace;
import org.jeo.feature.Schema;
import org.jeo.geotools.GT;
import org.opengis.feature.simple.SimpleFeatureType;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterators;

public class GeoGit implements Workspace {

    GeoGIT gg;

    public GeoGit(GeoGIT gg) {
        this.gg = gg;
    }

    public GeoGIT getGeoGIT() {
        return gg;
    }

    @Override
    public Iterator<String> layers() throws IOException {
        List<NodeRef> trees = typeRefs();
        return Iterators.transform(trees.iterator(), new Function<NodeRef, String>() {
            @Override
            public String apply(NodeRef input) {
                return NodeRef.nodeFromPath(input.path());
            }
        });
    }

    @Override
    public GeoGitDataset get(String layer) throws IOException {
        NodeRef ref = typeRef(layer);
        if (ref == null) {
            return null;
        }

        SimpleFeatureType featureType = featureType(ref);
        if (featureType != null) {
            return new GeoGitDataset(GT.schema(featureType), this);
        }

        throw new IllegalStateException("No schema for tree: " + layer); 
    }

    String rootRef() {
        Repository repo = gg.getRepository();

        //grab the head reference
        Optional<Ref> head = repo.command(RefParse.class).setName(Ref.HEAD).call();
        if (head.isPresent()) {
            Ref ref = head.get();
            if (ref instanceof SymRef) {
                String target = ((SymRef) ref).getTarget();
                if (target.startsWith(ref.HEADS_PREFIX)) {
                    return target.substring(Ref.HEADS_PREFIX.length());
                }
            }
        }

        return Ref.WORK_HEAD;
    }

    List<NodeRef> typeRefs() {
        Repository repo = gg.getRepository();

        String root = rootRef();
        return repo.command(FindFeatureTypeTrees.class).setRootTreeRef(root).call();
    }

    NodeRef typeRef(final String name) {
        Collection<NodeRef> match =  Collections2.filter(typeRefs(), new Predicate<NodeRef>() {
            @Override
            public boolean apply(NodeRef input) {
                return NodeRef.nodeFromPath(input.path()).equals(name);
            }
        }); 

        if (match.isEmpty()) {
            return null;
        }

        if (match.size() == 1) {
            return match.iterator().next();
        }

        throw new IllegalArgumentException("Multiple tree matches for " + name);
    }

    SimpleFeatureType featureType(NodeRef ref) {
        Optional<RevFeatureType> type = gg.command(RevObjectParse.class)
            .setObjectId(ref.getMetadataId()).call(RevFeatureType.class);
        return (SimpleFeatureType) (type.isPresent() ? type.get().type() : null);
    }

    @Override
    public Vector create(Schema schema) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub
        
    }
}
