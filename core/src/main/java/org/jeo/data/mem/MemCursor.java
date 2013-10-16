package org.jeo.data.mem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jeo.data.Cursor;
import org.jeo.data.Cursors;
import org.jeo.feature.BasicFeature;
import org.jeo.feature.DiffFeature;
import org.jeo.feature.Feature;

public class MemCursor extends Cursor<Feature> {

    MemVector dataset;
    Cursor<Feature> cursor;
    Feature curr;

    MemCursor(Mode mode, List<Feature> features, MemVector dataset) {
        super(mode);
        this.dataset = dataset;
        cursor = Cursors.create(features);
    }
    
    @Override
    public boolean hasNext() throws IOException {
        if (mode == APPEND) {
            return true;
        }
    
        return cursor.hasNext();
    }
    
    @Override
    public Feature next() throws IOException {
        if (mode == APPEND) {
            curr = new BasicFeature(null, new ArrayList<Object>(), dataset.schema());
        }
        else {
            curr = cursor.next();
            if (mode == UPDATE) {
                curr = new DiffFeature(curr);
            }
        }
        return curr;
    }
    
    @Override
    protected void doWrite() throws IOException {
        if (mode == APPEND) {
            dataset.add(curr);
        }
        else {
            dataset.modify((DiffFeature)curr);
        }
    }
    
    @Override
    protected void doRemove() throws IOException {
        Feature f = ((DiffFeature)curr).getDelegate();
        dataset.remove(f);
    }
    
    @Override
    public void close() throws IOException {
    }
}
