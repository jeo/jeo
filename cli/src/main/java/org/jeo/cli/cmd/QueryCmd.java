package org.jeo.cli.cmd;

import java.net.URI;
import java.util.List;
import java.util.Map;

import jline.console.ConsoleReader;

import org.jeo.cli.JeoCLI;
import org.jeo.data.Cursor;
import org.jeo.data.Cursors;
import org.jeo.data.Dataset;
import org.jeo.data.Drivers;
import org.jeo.data.Query;
import org.jeo.data.Tile;
import org.jeo.data.TileDataset;
import org.jeo.data.TileSetView;
import org.jeo.data.VectorDataset;
import org.jeo.feature.Feature;
import org.jeo.filter.Filter;
import org.jeo.geojson.GeoJSONWriter;
import org.jeo.geom.Envelopes;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.common.base.Strings;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;
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
        GeoJSONWriter w = new GeoJSONWriter(cli.getConsole().getOutput(), 2);

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

        GeoJSONWriter w = new GeoJSONWriter(cli.getConsole().getOutput(), 2);

        Cursor<Tile> cursor = new TileSetView(dataset).cursor(bbox, 1024, 1024);
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
