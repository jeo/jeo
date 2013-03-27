package org.jeo.geogit;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.geogit.api.GeoGIT;
import org.geogit.api.GeogitTransaction;
import org.geogit.api.NodeRef;
import org.geogit.api.Ref;
import org.geogit.api.RevCommit;
import org.geogit.api.RevFeatureType;
import org.geogit.api.SymRef;
import org.geogit.api.data.FindFeatureTypeTrees;
import org.geogit.api.plumbing.RefParse;
import org.geogit.api.plumbing.RevObjectParse;
import org.geogit.api.plumbing.TransactionBegin;
import org.geogit.api.porcelain.AddOp;
import org.geogit.api.porcelain.BranchListOp;
import org.geogit.api.porcelain.CheckoutOp;
import org.geogit.api.porcelain.CommitOp;
import org.geogit.api.porcelain.LogOp;
import org.geogit.repository.Repository;
import org.geogit.repository.WorkingTree;
import org.jeo.data.Workspace;
import org.jeo.data.Workspaces;
import org.jeo.feature.Schema;
import org.jeo.geotools.GT;
import org.jeo.util.Pair;
import org.opengis.feature.simple.SimpleFeatureType;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;

public class GeoGit implements Workspace {

    static {
        Workspaces.registerWorkspaceFactory(new GeoGitFactory());
    }

    /** underlying geogit repo */
    GeoGIT gg;

    public GeoGit(GeoGIT gg) {
        this.gg = gg;
    }

    /**
     * Underlying geogit repo.
     */
    public GeoGIT getGeoGIT() {
        return gg;
    }

    public Iterator<String> branches() {
        ImmutableList<Ref> heads = gg.command(BranchListOp.class).call();
        return Collections2.transform(heads, new Function<Ref,String>() {
            @Override
            public String apply(Ref ref) {
                return ref.getName().substring(Ref.HEADS_PREFIX.length());
            }
        }).iterator();
    }

    @Override
    public Iterator<String> layers() throws IOException {
        return layers(branch());
    }

    public Iterator<String> layers(String rev) throws IOException {
        List<NodeRef> trees = typeRefs(rev);
        return Iterators.transform(trees.iterator(), new Function<NodeRef, String>() {
            @Override
            public String apply(NodeRef input) {
                return NodeRef.nodeFromPath(input.path());
            }
        });
    }

    public Iterator<RevCommit> log(String branch, String... layers) throws IOException {
        LogOp log = gg.command(LogOp.class);
        for (String l : layers) {
            log.addPath(l);
        }

        return log.call();
    }

    @Override
    public GeoGitDataset get(String layer) throws IOException {
        Pair<NodeRef,RevCommit> ref = parseName(layer);
        if (ref == null) {
            return null;
        }

        SimpleFeatureType featureType = featureType(ref.first());
        if (featureType != null) {
            return new GeoGitDataset(ref, GT.schema(featureType), this);
        }

        throw new IllegalStateException("No schema for tree: " + layer); 
    }

    Pair<NodeRef,RevCommit> parseName(String name) {
        String[] split = name.split("@");

        final String type = split.length > 1 ? split[0] : name; 
        final String rev = split.length > 1 ? split[1] : branch();

        //parse the revision
        Optional<RevCommit> commit = 
            gg.command(RevObjectParse.class).setRefSpec(rev).call(RevCommit.class);
        if (!commit.isPresent()) {
            throw new IllegalArgumentException("No such commit: " + rev);
        }

        Collection<NodeRef> match =  Collections2.filter(typeRefs(rev), new Predicate<NodeRef>() {
            @Override
            public boolean apply(NodeRef input) {
                return NodeRef.nodeFromPath(input.path()).equals(type);
            }
        }); 

        if (match.isEmpty()) {
            return null;
        }

        if (match.size() == 1) {
            return new Pair<NodeRef,RevCommit>(match.iterator().next(), commit.get());
        }

        throw new IllegalArgumentException("Multiple trees for " + type);
    }

    /**
     * Returns the ref name <tt>ref</tt> if not null, otherwise falls back to name of current branch.
     */
    String refOrBranch(String ref) {
        return ref != null ? ref : branch();
    }

    /**
     * Determines the "current" branch by parsing the HEAD reference.
     */
    String branch() {
        //grab the current branch
        Repository repo = gg.getRepository();

        //grab the head reference
        Optional<Ref> head = repo.command(RefParse.class).setName(Ref.HEAD).call();
        if (head.isPresent()) {
            Ref ref = head.get();
            if (ref instanceof SymRef) {
                String target = ((SymRef) ref).getTarget();
                if (target.startsWith(Ref.HEADS_PREFIX)) {
                    return target.substring(Ref.HEADS_PREFIX.length());
                }
            }
        }

        throw new IllegalStateException(
            "Unable to determine current branch, repository in dettached head state");
    }

    /**
     * Lists all the type references for the specified branch/revision. 
     */
    List<NodeRef> typeRefs(String rev) {
        Repository repo = gg.getRepository();
        return repo.command(FindFeatureTypeTrees.class).setRootTreeRef(refOrBranch(rev)).call();
    }

    SimpleFeatureType featureType(NodeRef ref) {
        Optional<RevFeatureType> type = gg.command(RevObjectParse.class)
            .setObjectId(ref.getMetadataId()).call(RevFeatureType.class);
        return (SimpleFeatureType) (type.isPresent() ? type.get().type() : null);
    }

    @Override
    public GeoGitDataset create(Schema schema) throws IOException {
        GeogitTransaction tx = gg.command(TransactionBegin.class).call();
        boolean abort = false;
        try {
            String treePath = schema.getName();
            String branch = branch();
            
            // check out the datastore branch on the transaction space
            tx.command(CheckoutOp.class).setForce(true).setSource(branch).call();

            // now we can use the transaction working tree with the correct branch checked out
            WorkingTree workingTree = tx.getWorkingTree();
            workingTree.createTypeTree(treePath, GT.featureType(schema));

            tx.command(AddOp.class).addPattern(treePath).call();
            tx.command(CommitOp.class).setMessage("Created type tree " + treePath).call();
            tx.commit();

            return get(schema.getName());
        } catch (IllegalArgumentException alreadyExists) {
            abort = true;
            throw new IOException(alreadyExists.getMessage(), alreadyExists);
        } catch (Exception e) {
            abort = true;
            throw Throwables.propagate(e);
        } finally {
            if (abort) {
                tx.abort();
            }
        }
    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub
        
    }
}
