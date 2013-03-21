package org.jeo.geogit;

import java.io.IOException;
import java.util.Iterator;

import org.geogit.api.GeoGIT;
import org.jeo.data.Dataset;
import org.jeo.data.Vector;
import org.jeo.data.Workspace;
import org.jeo.feature.Schema;

public class GeoGit implements Workspace {

    GeoGIT gg;

    public GeoGit(GeoGIT gg) {
        this.gg = gg;
    }

    @Override
    public Iterator<String> layers() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Dataset get(String layer) throws IOException {
        // TODO Auto-generated method stub
        return null;
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
