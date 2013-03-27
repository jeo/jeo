package org.jeo.data.mem;

import java.io.IOException;
import java.util.ArrayList;

import org.jeo.data.Cursor;
import org.jeo.data.Cursors;
import org.jeo.feature.DiffFeature;
import org.jeo.feature.Feature;
import org.jeo.feature.ListFeature;

public class MemoryCursor extends Cursor<Feature> {

    MemoryVector dataset;
    Cursor<Feature> cursor;
    Feature curr;

    MemoryCursor(Mode mode, MemoryVector dataset) {
        super(mode);
        this.dataset = dataset;
        cursor = Cursors.create(dataset.getFeatures());
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
            curr = new ListFeature(null, new ArrayList(), dataset.getSchema());
        }
        else {
            curr = new DiffFeature(cursor.next()); 
        }
        return curr;
    }
    
    @Override
    protected void doWrite() throws IOException {
        if (mode == APPEND) {
            dataset.getFeatures().add(curr);
        }
        else {
            ((DiffFeature) curr).apply();
        }
    }
    
    @Override
    protected void doRemove() throws IOException {
        dataset.getFeatures().remove(((DiffFeature)curr).getDelegate());
    }
    
    @Override
    public void close() throws IOException {
    }
}
