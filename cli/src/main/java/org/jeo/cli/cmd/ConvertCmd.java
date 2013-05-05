package org.jeo.cli.cmd;

import java.net.URI;
import java.util.List;

import org.jeo.cli.ConsoleProgress;
import org.jeo.cli.JeoCLI;
import org.jeo.data.Cursor;
import org.jeo.data.Dataset;
import org.jeo.data.Disposable;
import org.jeo.data.Drivers;
import org.jeo.data.Query;
import org.jeo.data.VectorData;
import org.jeo.data.Workspace;
import org.jeo.feature.Feature;
import org.jeo.feature.Features;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandNames="convert", commandDescription="Converts between data sources")
public class ConvertCmd extends JeoCmd {

    @Parameter(description="from to", arity = 2, required=true)
    List<String> datas;
    
    @Override
    protected void doCommand(JeoCLI cli) throws Exception {
        Object from = Drivers.open(parseDataURI(datas.get(0)));
        if (!(from instanceof VectorData)) {
            throw new IllegalArgumentException("from must be a vector dataset");
        }

        VectorData orig = open((VectorData) from);

        URI uri = parseDataURI(datas.get(1));

        VectorData dest = null;
        
        //first see if dest is a workspace
        Object to = null;
        try {
            to = open((Disposable)Drivers.open(uri));
        }
        catch(Exception e) {
            //TODO: log
        }
        if (to instanceof Dataset) {
            throw new IllegalArgumentException("Destination dataset already exists");
        }

        if (to == null) {
            //see if we can create a new dataset directly
            dest = Drivers.create(orig.getSchema(), uri, VectorData.class);
            if (dest == null) {
                throw new IllegalArgumentException("Unable to create dataset: " + uri);
            }
        }
        else if (to instanceof Workspace) {
            dest = open((Workspace)to).create(orig.getSchema());
        }
        else {
            throw new IllegalArgumentException("Invalid destination: " + uri);
        }

        Cursor<Feature> o = null, d = null;
        try {

            ConsoleProgress progress = 
                new ConsoleProgress(cli.getConsole(), (int) orig.count(new Query()));

            o = orig.cursor(new Query());
            d = dest.cursor(new Query().append());
    
            while(o.hasNext()) {
                Feature a = o.next();
                Feature b = d.next();
    
                Features.copy(a, b);
                d.write();

                progress.progress(1);
            }
        }
        finally {
            if (o != null) o.close();
            if (d != null) d.close();
        }

    }

}
