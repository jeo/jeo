package org.jeo.cli.cmd;

import java.io.File;
import java.io.IOException;
import java.util.List;

import jline.console.ConsoleReader;

import org.jeo.cli.JeoCLI;
import org.jeo.data.DirectoryRegistry;
import org.jeo.data.Registry;
import org.jeo.data.SimpleRegistry;
import org.jeo.nano.NanoJeoServer;
import org.jeo.util.Util;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandNames="serve", commandDescription="Starts NanoHTTPD web server")
public class ServeCmd extends JeoCmd {

    @Parameter(description="registry", arity=1, required=true)
    List<String> reg;

    @Parameter(names = {"-p", "-port" }, description="Port to listen on")
    Integer port = 8000;

    @Override
    protected void doCommand(JeoCLI cli) throws Exception {
        ConsoleReader console = cli.getConsole();
        console.println("Starting NanoHTTPD on port " + port);
        console.println("Serving data from " + reg);
        console.flush();

        File f = new File(reg.get(0));
        Registry registry = registry(f, cli);
        
        NanoJeoServer server = new NanoJeoServer(port, null, registry);
        server.join();

    }

    Registry registry(File f, JeoCLI cli) throws IOException {
        if (f.isDirectory()) {
            return new DirectoryRegistry(f);
        }

        ConsoleReader console = cli.getConsole();
        console.println("Unable to process " + f);
        console.flush();

        return new SimpleRegistry();
    }
}
