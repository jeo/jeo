package org.jeo.cli.cmd;

import java.util.List;
import java.util.Map;

import jline.console.ConsoleReader;

import org.jeo.cli.JeoCLI;
import org.jeo.cli.conv.MapConverter;
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
    
    @Parameter(names = "-bbox", description = "Bounding box (xmin,ymin,xmax,ymax)")
    Envelope bbox;

    @Parameter(names = "-filter", description = "Predicate used to constrain results")
    Filter filter;

    @Parameter(names = "-count", description = "Maximum number of results to return")
    Integer count;

    @Parameter(names = "-summary", description = "Summarize results only")
    boolean summary;
    
    @Override
    protected void doCommand(JeoCLI cli) throws Exception {
        ConsoleReader console = cli.getConsole();

        for (String data : datas) {
            Map<String,Object> map = new MapConverter().convert(data);

            VectorData dataset = Drivers.open(map, VectorData.class);
            try {
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
    
                Schema schema = dataset.getSchema();
    
                int size = new Ordering<Field>() {
                    public int compare(Field left, Field right) {
                        return Ints.compare(left.getName().length(), right.getName().length());
                    };
                }.max(schema.getFields()).getName().length() + 2;

                if (summary) {
                    console.println(String.format("Query matched %d features", dataset.count(q)));
                }
                else {
                    for (Feature f : dataset.cursor(q)) {
                        console.println("Feature " + f.getId());
                        for (Field fld : schema) {
                            console.print(Strings.padStart(fld.getName(), size, ' '));
                            console.println("\t=\t" + f.get(fld.getName()));
                        }
                        console.println();
                    }
                }
            }
            finally {
                dataset.dispose();
            }
            
        }
    }

}
