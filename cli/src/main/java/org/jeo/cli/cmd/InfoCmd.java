package org.jeo.cli.cmd;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.jeo.cli.JeoCLI;
import org.jeo.data.DataRef;
import org.jeo.data.Dataset;
import org.jeo.data.DirectoryRegistry;
import org.jeo.data.Drivers;
import org.jeo.data.Query;
import org.jeo.data.TileDataset;
import org.jeo.data.TileGrid;
import org.jeo.data.TilePyramid;
import org.jeo.data.VectorDataset;
import org.jeo.data.Workspace;
import org.jeo.feature.Field;
import org.jeo.geojson.GeoJSONWriter;
import org.jeo.geom.Envelopes;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.vividsolutions.jts.geom.Envelope;

@Parameters(commandNames="info", commandDescription="Provides information about a data source")
public class InfoCmd extends JeoCmd {

    @Parameter(description="datasource", required=true)
    List<String> datas;

    @Override
    protected void doCommand(JeoCLI cli) throws Exception {
        GeoJSONWriter w = new GeoJSONWriter(cli.getConsole().getOutput(), 2);

        for (String data : datas) {
            URI uri = parseDataURI(data);

            try {
                Object obj = Drivers.open(uri);
                if (obj == null) {
                    throw new IllegalArgumentException("Unable to open data source: " + uri);
                }
    
                print(obj, w, cli);
            }
            catch(Exception e) {
                File f = new File(uri);
                if (f.exists() && f.isDirectory()) {
                    DirectoryRegistry reg = new DirectoryRegistry(f);
                    try {
                        for (DataRef<?> it : reg.list()) {
                            print(reg.get(it.getName()), w, cli);
                        }
                    }
                    finally {
                        reg.close();
                    }
                }
                else {
                    throw e;
                }
            }
        }
    }

    void print(Object obj, GeoJSONWriter w, JeoCLI cli) throws IOException {
        if (obj instanceof Workspace) {
            print((Workspace)obj, w, cli);
        }
        else if (obj instanceof VectorDataset) {
            print((VectorDataset)obj, w, cli);
        }
        else if (obj instanceof TileDataset) {
            print((TileDataset)obj, w, cli);
        }
        else {
            throw new IllegalArgumentException(
                "Object " + obj.getClass().getName() + " not supported");
        }
    }

    void print(Dataset dataset, GeoJSONWriter w, JeoCLI cli) throws IOException {
        
         w.key("name").value(dataset.getName());
         w.key("driver").value(dataset.getDriver().getName());
  
         Envelope bbox = dataset.bounds();
         if (!Envelopes.isNull(bbox)) {
             w.key("bbox");
             w.bbox(bbox);
         }
  
          CoordinateReferenceSystem crs = dataset.crs();
          if (crs != null) {
             w.key("crs");
             print(crs, w, cli);
          }
    }

    void print(VectorDataset dataset, GeoJSONWriter w, JeoCLI cli) throws IOException {
        w.object();
        w.key("type").value("vector");
    
        try {
            print((Dataset) dataset, w, cli);
    
            w.key("count").value(dataset.count(new Query()));
            w.key("schema").object();
    
            for (Field fld : dataset.schema()) {
                w.key(fld.getName()).value(fld.getType().getSimpleName());
            }

            w.endObject();
        } finally {
            dataset.close();
        }
    
        w.endObject();
    }

    void print(TileDataset dataset, GeoJSONWriter w, JeoCLI cli) throws IOException {
        w.object();
        w.key("type").value("tileset");
    
        try {
            print((Dataset) dataset, w, cli);
    
            TilePyramid pyr = dataset.pyramid();
            w.key("tilesize").array().value(pyr.getTileWidth())
                    .value(pyr.getTileHeight()).endArray();
    
            w.key("grids").array();
            for (TileGrid grid : dataset.pyramid().getGrids()) {
                w.object().key("zoom").value(grid.getZ()).key("width")
                    .value(grid.getWidth()).key("height")
                    .value(grid.getHeight()).key("res").array()
                    .value(grid.getXRes()).value(grid.getYRes()).endArray()
                    .endObject();
    
            }
            w.endArray();
        } finally {
            dataset.close();
        }
    
        w.endObject();
    }

    void print(Workspace workspace, GeoJSONWriter w, JeoCLI cli) throws IOException {
        w.object();
    
        try {
            w.key("type").value("workspace");
            w.key("driver").value(workspace.getDriver().getName());
            w.key("datasets").array();
    
            for (DataRef<? extends Dataset> ref : workspace.list()) {
                w.value(ref.getName());
            }
    
            w.endArray();
        } finally {
            workspace.close();
        }
    
        w.endObject();
    }

    void print(CoordinateReferenceSystem crs, GeoJSONWriter w, JeoCLI cli) throws IOException {
        w.array();
    
        for (String s : crs.getParameters()) {
            w.value(s);
        }
    
        w.endArray();
    }
}
