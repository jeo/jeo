package org.jeo.cli.cmd;

import java.net.URI;
import java.util.List;
import java.util.Map;

import jline.console.ConsoleReader;

import org.jeo.cli.JeoCLI;
import org.jeo.data.Drivers;
import org.jeo.data.Query;
import org.jeo.data.VectorData;
import org.jeo.feature.Feature;
import org.jeo.feature.Field;
import org.jeo.feature.Schema;
import org.jeo.filter.Filter;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.common.base.Strings;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;
import com.vividsolutions.jts.geom.Envelope;

@Parameters(commandNames="query", commandDescription="Executes a query against a data source")
public class QueryCmd extends JeoCmd {

    @Parameter(description="data", arity = 1, required=true)
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
        ConsoleReader console = cli.getConsole();

        for (String data : datas) {
            URI uri = parseDataURI(data);

            VectorData dataset = null;
            try {
                dataset = open((VectorData)Drivers.open(uri));
            }
            catch(ClassCastException e) {
                throw new IllegalArgumentException(data + " is not a vector dataset");
            }

            if (dataset == null) {
                throw new IllegalArgumentException("Unable to open data source: " + data);
            }

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
                console.println(String.format("Query matched %d features", dataset.count(q)));
            }
            else {
                for (Feature f : dataset.cursor(q)) {
                    console.println("Feature " + f.getId());
                    Map<String,Object> map = f.map();

                    int padd = new Ordering<String>() {
                        @Override
                        public int compare(String left, String right) {
                            return Ints.compare(left.length(), right.length());
                        }
                    }.max(map.keySet()).length();

                    for (Map.Entry<String, Object> kv : f.map().entrySet()) {
                        console.print(Strings.padStart(kv.getKey(), padd, ' '));
                        console.println("\t=\t" + kv.getValue());
                    }

                    console.println();
                }
            }
        }
    }

}
