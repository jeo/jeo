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
package org.jeo.map;

import java.io.IOException;
import java.util.Iterator;

import org.jeo.data.Cursor;
import org.jeo.data.Dataset;
import org.jeo.data.Drivers;
import org.jeo.data.Workspace;
import org.jeo.data.mem.MemVector;
import org.jeo.feature.Feature;
import org.jeo.filter.Filter;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;

public class MapBuilder {

    static Logger LOG = LoggerFactory.getLogger(MapBuilder.class);

    Map map;
    View view;
   
    boolean size = false, bounds = false, crs = false;

    public MapBuilder() {
        map = new Map();
        view = new View(map);
    }

    public MapBuilder size(int width, int height) {
        view.setWidth(width);
        view.setHeight(height);
        size = true;
        return this;
    }

    public MapBuilder bounds(Envelope bounds) {
        view.setBounds(bounds);
        this.bounds = true;
        return this;
    }

    public MapBuilder bounds(double x1, double y1, double x2, double y2) {
        return bounds(new Envelope(x1,x2,y1,y2));
    }

    public MapBuilder crs(CoordinateReferenceSystem crs) {
        view.setCRS(crs);
        this.crs = true;
        return this;
    }

    public MapBuilder layer(Dataset data) {
        return layer(data.getName(), data);
    }

    public MapBuilder layer(Dataset data, Filter<Object> filter) {
        return layer(data.getName(), data, filter);
    }

    public MapBuilder layer(String name, Dataset data) {
        return layer(name, name, data, null);
    }

    public MapBuilder layer(String name, Dataset data, Filter<Object> filter) {
        return layer(name, name, data, filter);
    }
    
    public MapBuilder layer(String name, String title, Dataset data, Filter<Object> filter) {
        Layer l = new Layer();
        l.setName(name);
        l.setTitle(title);
        l.setData(data);
        l.setFilter(filter);

        map.getLayers().add(l);
        return this;
    }

    public MapBuilder layer(String name, java.util.Map<String,Object> params) throws IOException {
        return layer(name, name, params);
    }

    public MapBuilder layer(String name, String title, java.util.Map<String,Object> params) 
        throws IOException {

        Workspace ws = Drivers.open(params, Workspace.class);
        if (ws == null) {
            throw new IllegalArgumentException("Unable to obtain workspace: " + params);
        }

        Dataset data = ws.get(name);
        if (data == null) {
            ws.close();
            throw new IllegalArgumentException(
                "No dataset named " + name + " in worksoace: " + params);
        }

        layer(name, title, data, null);
        map.getCleanup().add(ws);
        return this;
    }

    public MapBuilder layer(Cursor<Feature> cursor) throws IOException {
        Feature first = null;
        if (cursor.hasNext()) {
            first = cursor.next();
        }

        if (first == null) {
            //nothing to render
            return this;
        }

        MemVector mem = new MemVector(first.schema());
        mem.add(first);

        try {
            for (Feature f : cursor) {
                mem.add(f);
            }
        } finally {
            cursor.close();
        }

        return layer(mem);
    }

    public MapBuilder style(Style style) {
        map.setStyle(style);
        return this;
    }

    public Map map() {
        if (!bounds || !crs) {
            //set from layers
            Iterator<Layer> it = map.getLayers().iterator();
            while(it.hasNext() && !bounds && !crs) {
                Layer l = it.next();
                try {
                    if (!bounds) {
                        Envelope e = l.getData().bounds();
                        if (e != null && !e.isNull()) {
                            view.setBounds(e);
                            bounds = true;
                        }
                    }
                    if (!crs) {
                        CoordinateReferenceSystem c = l.getData().crs();
                        if (c != null) {
                            view.setCRS(c);
                            crs = true;
                        }
                    }
                } catch (IOException ex) {
                    LOG.debug("Error deriving bounds/crs from map layers", ex);
                }
            }
        }

        if (!size) {
            //set from bounds
            Envelope e = view.getBounds();
            if (e != null) {
                view.setWidth(View.DEFAULT_WIDTH);
                view.setHeight((int)(view.getWidth() * e.getHeight() / e.getWidth()));
            }
        }

        return map;
    }

    public View view() {
        map();
        return view;
    }
}
