package org.jeo.cli.cmd;

import com.beust.jcommander.Parameters;
import org.jeo.cli.JeoCLI;
import org.jeo.protobuf.ProtobufReader;
import org.jeo.vector.Feature;
import org.jeo.vector.Schema;

@Parameters(commandNames="query", commandDescription="Executes a query against a data set")
public class HackCmd extends JeoCmd{
    @Override
    protected void run(JeoCLI cli) throws Exception {
        ProtobufReader r = new ProtobufReader(System.in);
        Schema schema = r.schema();

        Feature f = null;
        while ((f = r.feature(schema)) != null) {
            System.out.println(f);
        }
    }
}
