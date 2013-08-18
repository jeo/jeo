package org.jeo.cli.cmd;

import java.net.URI;
import java.util.List;

import org.jeo.cli.ConsoleProgress;
import org.jeo.cli.JeoCLI;
import org.jeo.data.Cursor;
import org.jeo.data.Cursors;
import org.jeo.data.Dataset;
import org.jeo.data.Disposable;
import org.jeo.data.Drivers;
import org.jeo.data.Query;
import org.jeo.data.Transaction;
import org.jeo.data.Transactional;
import org.jeo.data.VectorData;
import org.jeo.data.Workspace;
import org.jeo.feature.Feature;
import org.jeo.feature.Features;
import org.jeo.feature.Schema;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandNames="convert", commandDescription="Converts between data sets")
public class ConvertCmd extends JeoCmd {

    @Parameter(description="source target", arity = 2, required=true)
    List<String> datas;

    @Parameter(names = { "-fc", "--from-crs"}, description="Source CRS override")
    CoordinateReferenceSystem fromCRS;

    @Parameter(names = { "-tc", "--to-crs"}, description="Target CRS")
    CoordinateReferenceSystem toCRS;

    @Parameter(names = {"--multify"}, description="Wrap single geometry objects in collection")
    boolean multify = false;

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
            if (debug) {
                print(e, cli);
            }
        }
        if (to instanceof Dataset) {
            throw new IllegalArgumentException("Destination dataset already exists");
        }

        Schema schema = orig.getSchema();
        if (multify) {
            schema = Features.multify(schema);
        }

        if (to == null) {
            //see if we can create a new dataset directly
            dest = Drivers.create(schema, uri, VectorData.class);
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

        Cursor<Feature> o = null, d = null;
        try {

            ConsoleProgress progress = 
                new ConsoleProgress(cli.getConsole(), (int) orig.count(new Query()));

            // create query for source
            Query q = new Query();

            // reprojection
            if (toCRS != null) {
                if (fromCRS == null && orig.getCRS() == null) {
                    throw new IllegalArgumentException(
                        "Could not determine source crs, must supply it with --src-crs");
                }

                q.reproject(fromCRS, toCRS);
            }
            o = orig.cursor(q);

            //multification
            if (multify) {
                o = Cursors.multify(o);
            }

            Transaction tx = null;
            if (dest instanceof Transactional) {
                tx = ((Transactional) dest).transaction(null);
            }

            d = dest.cursor(new Query().append().transaction(tx));

            try {
                while(o.hasNext()) {
                    Feature a = o.next();
                    Feature b = d.next();
        
                    Features.copy(a, b);
                    d.write();
    
                    progress.progress(1);
                }
    
                if (tx != null) {
                    tx.commit();
                }
            }
            catch(Exception e) {
                if (tx != null) tx.rollback();
                throw e;
            }
        }
        finally {
            if (o != null) o.close();
            if (d != null) d.close();
        }

    }

}
