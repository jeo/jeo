/* Copyright 2013 The jeo project. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jeo.cli.cmd;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.jeo.cli.JeoCLI;
import org.jeo.data.Dataset;
import org.jeo.data.DirectoryRepository;
import org.jeo.data.Drivers;
import org.jeo.data.Handle;
import org.jeo.data.Query;
import org.jeo.data.TileDataset;
import org.jeo.data.TileGrid;
import org.jeo.data.TilePyramid;
import org.jeo.data.VectorDataset;
import org.jeo.data.Workspace;
import org.jeo.feature.Field;
import org.jeo.filter.Filters;
import org.jeo.geojson.GeoJSONWriter;
import org.jeo.geom.Envelopes;
import org.jeo.map.Style;
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
        GeoJSONWriter w = cli.newGeoJSONWriter();

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
                // try to parse input as file
                File f = null;
                try {
                    f = new File(uri);
                }
                catch(Exception e2) {}

                if (f != null && f.exists() && f.isDirectory()) {
                    DirectoryRepository reg = new DirectoryRepository(f);
                    try {
                        for (Handle<?> h : reg.query(Filters.all())) {
                            print(h.resolve(), w, cli);
                        }
                    }
                    finally {
                        reg.close();
                    }
                }
                else {
                    // no luck, return original exception
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
        else if (obj instanceof Style) {
            print((Style)obj, w, cli);
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
        w.key("type").value("tile");
    
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
    
            for (Handle<Dataset> h : workspace.list()) {
                w.value(h.getName());
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

    void print(Style style, GeoJSONWriter w, JeoCLI cli) throws IOException {
        cli.getConsole().getOutput().write(style.toString());
    }
}
