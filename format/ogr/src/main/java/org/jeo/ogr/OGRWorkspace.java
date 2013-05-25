package org.jeo.ogr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.gdal.ogr.DataSource;
import org.gdal.ogr.Layer;
import org.jeo.data.Dataset;
import org.jeo.data.Driver;
import org.jeo.data.VectorData;
import org.jeo.data.Workspace;
import org.jeo.feature.Schema;

public class OGRWorkspace implements Workspace {

    DataSource data;
    OGRDriver driver;

    public OGRWorkspace(DataSource data, OGRDriver driver) {
        this.data = data;
        this.driver = driver;
    }

    @Override
    public Driver<?> getDriver() {
        return driver;
    }

    @Override
    public Iterator<String> layers() throws IOException {
        List<String> names = new ArrayList<String>();
        for (int i = 0; i < data.GetLayerCount(); i++) {
            Layer l = data.GetLayer(i);
            names.add(l.GetName());
            l.delete();
        }
        return names.iterator();
    }

    @Override
    public Dataset get(String layer) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public VectorData create(Schema schema) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub
        
    }

}
