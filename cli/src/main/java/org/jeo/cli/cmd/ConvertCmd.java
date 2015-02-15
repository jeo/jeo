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

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.jeo.cli.ConsoleProgress;
import org.jeo.cli.JeoCLI;
import org.jeo.data.Cursor;
import org.jeo.data.Cursors;
import org.jeo.data.Dataset;
import org.jeo.data.Disposable;
import org.jeo.data.Drivers;
import org.jeo.vector.FeatureCursor;
import org.jeo.vector.VectorQuery;
import org.jeo.data.Transaction;
import org.jeo.data.Transactional;
import org.jeo.vector.VectorDataset;
import org.jeo.data.Workspace;
import org.jeo.vector.Feature;
import org.jeo.vector.Features;
import org.jeo.vector.Schema;
import org.jeo.filter.Filter;
import org.jeo.geojson.GeoJSONWriter;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.vividsolutions.jts.geom.Envelope;

@Parameters(commandNames="convert", commandDescription="Converts between data sets")
public class ConvertCmd extends JeoCmd {

    @Parameter(description="source target", arity = 2, required=true)
    List<String> datas;

    @Parameter(names = {"-b", "--bbox"}, description = "Bounding box (xmin,ymin,xmax,ymax)")
    Envelope bbox;

    @Parameter(names = {"-f", "--filter"}, description = "Predicate used to constrain results")
    Filter filter;

    @Parameter(names = { "-fc", "--from-crs"}, description="Source CRS override")
    CoordinateReferenceSystem fromCRS;

    @Parameter(names = { "-tc", "--to-crs"}, description="Target CRS")
    CoordinateReferenceSystem toCRS;

    @Parameter(names = {"-mu", "--multify"}, description="Wrap single geometry objects in collection")
    boolean multify = false;

    @Override
    protected void doCommand(JeoCLI cli) throws Exception {
        Object from = Drivers.open(parseDataURI(datas.get(0)));
        if (!(from instanceof VectorDataset)) {
            throw new IllegalArgumentException("from must be a vector dataset");
        }

        VectorDataset orig = open((VectorDataset) from);
        Schema schema = orig.schema();

        // choose a sink, if user specified a destination dataset copy over otherwise just
        // output as geojson
        Sink sink = null;

        if (datas.size() > 1) {
            URI uri = parseDataURI(datas.get(1));
    
            VectorDataset dest = null;
            
            //first see if dest is a workspace
            Object to = null;
            try {
                to = open((Disposable)Drivers.open(uri));
            }
            catch(Exception e) {
                if (debug) {
                    print(e, cli);
                }
            }
            if (to instanceof Dataset) {
                throw new IllegalArgumentException("Destination dataset already exists");
            }

            if (to == null) {
                //see if we can create a new dataset directly
                if (uri.getFragment() != null) {
                    schema = Schema.build(uri.getFragment()).fields(schema.getFields()).schema();
                }
                dest = Drivers.create(schema, uri, VectorDataset.class);
                if (dest == null) {
                    throw new IllegalArgumentException("Unable to create dataset: " + uri);
                }
            }
            else if (to instanceof Workspace) {
                dest = open((Workspace)to).create(schema);
            }
            else {
                throw new IllegalArgumentException("Invalid destination: " + uri);
            }
            
            sink = new ToDataset(dest, orig);
        }
        else {
            sink = new ToGeoJSON();
        }
        
        if (multify) {
            schema = Features.multify(schema);
        }

        // create query for source
        VectorQuery q = new VectorQuery();

        if (bbox != null) {
            //TODO: make it clear that bbox is in source data coordinates
            q.bounds(bbox);
        }
        if (filter != null) {
            q.filter(filter);
        }

        // reprojection
        if (toCRS != null) {
            if (fromCRS == null && orig.crs() == null) {
                throw new IllegalArgumentException(
                    "Could not determine source crs, must supply it with --src-crs");
            }

            q.reproject(fromCRS, toCRS);
        }

        FeatureCursor o = null;
        try {
            o = orig.cursor(q);

            //multification
            if (multify) {
                o = o.multify();
            }

            sink.start(orig, cli);

            try {
                while(o.hasNext()) {
                    sink.handle(o.next(), cli);
                }
    
                sink.finish(cli);
            }
            catch(Exception e) {
                sink.error(e, cli);
                throw e;
            }
        }
        finally {
            if (o != null) o.close();
            sink.cleanup(cli);
        }

    }

    interface Sink {
        void start(VectorDataset data, JeoCLI cli) throws IOException;

        void handle(Feature f, JeoCLI cli) throws IOException;

        void error(Exception e, JeoCLI cli) throws IOException;

        void finish(JeoCLI cli) throws IOException;

        void cleanup(JeoCLI cli) throws IOException;
    }
    
    class ToDataset implements Sink {

        VectorDataset to;
        ConsoleProgress progress;
        Transaction tx;
        Cursor<Feature> d;

        ToDataset(VectorDataset to, VectorDataset from) {
            this.to = to;
        }

        @Override
        public void start(VectorDataset data, JeoCLI cli) throws IOException {
            progress = new ConsoleProgress(cli.getConsole(), (int) data.count(new VectorQuery()));

            if (to instanceof Transactional) {
                tx = ((Transactional) to).transaction(null);
            }

            d = to.cursor(new VectorQuery().append().transaction(tx));
        }

        @Override
        public void handle(Feature f, JeoCLI cli) throws IOException {
            Feature b = d.next();
            
            Features.copy(f, b);
            d.write();

            progress.progress(1);
        }

        @Override
        public void error(Exception e, JeoCLI cli) throws IOException {
            if (tx != null) {
                tx.rollback();
            }
        }

        @Override
        public void finish(JeoCLI cli) throws IOException {
            if (tx != null) {
                tx.commit();
            }
        }

        @Override
        public void cleanup(JeoCLI cli) throws IOException {
            if (d != null) {
                d.close();
                d = null;
            }
        }
    }

    class ToGeoJSON implements Sink {

        GeoJSONWriter out;

        @Override
        public void start(VectorDataset data, JeoCLI cli) throws IOException {
            out = cli.newJSONWriter();
            out.featureCollection();
        }

        @Override
        public void handle(Feature f, JeoCLI cli) throws IOException {
            out.feature(f);
        }

        @Override
        public void error(Exception e, JeoCLI cli) throws IOException {
        }

        @Override
        public void finish(JeoCLI cli) throws IOException {
            out.endFeatureCollection();
        }
    
        @Override
        public void cleanup(JeoCLI cli) throws IOException {
            out.flush();
        }
    }
}
