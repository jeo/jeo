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

import java.net.URI;
import java.util.List;

import org.jeo.cli.JeoCLI;
import org.jeo.data.Cursor;
import org.jeo.data.Cursors;
import org.jeo.data.Dataset;
import org.jeo.data.Drivers;
import org.jeo.data.Query;
import org.jeo.data.TileDataset;
import org.jeo.data.TileDataView;
import org.jeo.data.VectorDataset;
import org.jeo.filter.Filter;
import org.jeo.geojson.GeoJSONWriter;
import org.jeo.tile.Tile;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.vividsolutions.jts.geom.Envelope;

@Parameters(commandNames="query", commandDescription="Executes a query against a data set")
public class QueryCmd extends JeoCmd {

    @Parameter(description="dataset", arity = 1, required=true)
    List<String> datas; 
    
    @Parameter(names = {"-b", "--bbox"}, description = "Bounding box (xmin,ymin,xmax,ymax)")
    Envelope bbox;

    @Parameter(names = {"-f", "--filter"}, description = "Predicate used to constrain results")
    Filter filter;

    @Parameter(names = {"-c", "-count" }, description = "Maximum number of results to return")
    Integer count;

    @Parameter(names = {"-s", "-summary"}, description = "Summarize results only")
    boolean summary;
    
    @Override
    protected void doCommand(JeoCLI cli) throws Exception {
        for (String data : datas) {
            URI uri = parseDataURI(data);

            Dataset dataset = null;
            try {
                dataset = open((Dataset)Drivers.open(uri));
            }
            catch(ClassCastException e) {
                throw new IllegalArgumentException(data + " is not a dataset");
            }

            if (dataset == null) {
                throw new IllegalArgumentException("Unable to open data source: " + data);
            }

            if (dataset instanceof VectorDataset) {
                query((VectorDataset)dataset, cli);
            }
            else {
                query((TileDataset)dataset, cli);
            }

        }
    }

    void query(VectorDataset dataset, JeoCLI cli) throws Exception {
        GeoJSONWriter w = cli.newJSONWriter();

        Query q = new Query();
        if (bbox != null) {
            q.bounds(bbox);
        }
        if (filter != null) {
            q.filter(filter);
        }
        if (count != null) {
            q.limit(count);
        }

        if (summary) {
            w.object().key("count").value(dataset.count(q)).endObject();
        }
        else {
            w.featureCollection(dataset.cursor(q));
        }
    }

    void query(TileDataset dataset, JeoCLI cli) throws Exception {
        if (bbox == null) {
            throw new IllegalArgumentException("Tile query must specify bbox");
        }

        GeoJSONWriter w = cli.newJSONWriter();

        Cursor<Tile> cursor = new TileDataView(dataset).cursor(bbox, 1024, 1024);
        if (count != null) {
            cursor = Cursors.limit(cursor, count);
        }

        if (summary) {
            w.object().key("count").value(Cursors.size(cursor)).endObject();
        }
        else {
            w.array();
            for (Tile t : cursor) {
                w.array().value(t.getZ()).value(t.getX()).value(t.getY());
            }
            w.endArray();
        }
    }
}
