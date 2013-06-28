package org.jeo.cli.cmd;

import java.io.File;
import java.util.List;

import jline.console.ConsoleReader;

import org.jeo.cli.JeoCLI;
import org.jeo.data.DirectoryRegistry;
import org.jeo.nano.NanoJeoServer;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandNames="serve", commandDescription="Starts ")
public class ServeCmd extends JeoCmd {

    @Parameter(description="registry", arity=1, required=true)
    List<String> reg;

    @Parameter(names = {"-p", "-port" }, description="port")
    Integer port = 8000;

    @Override
    protected void doCommand(JeoCLI cli) throws Exception {
        ConsoleReader console = cli.getConsole();
        console.println("Starting NanoHTTPD on port " + port);
        console.println("Serving data from " + reg);
        console.flush();

        DirectoryRegistry registry = new DirectoryRegistry(new File(reg.get(0)));
        NanoJeoServer server = new NanoJeoServer(port, null, registry);
        server.join();

    }
}
