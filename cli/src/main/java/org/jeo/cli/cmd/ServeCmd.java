package org.jeo.cli.cmd;

import java.io.File;
import java.io.IOException;
import java.util.List;

import jline.console.ConsoleReader;

import org.jeo.cli.JeoCLI;
import org.jeo.data.DirectoryRepository;
import org.jeo.data.DataRepository;
import org.jeo.data.JSONRepository;
import org.jeo.nano.NanoServer;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandNames="serve", commandDescription="Starts NanoHTTPD web server")
public class ServeCmd extends JeoCmd {

    @Parameter(description="registry", arity=1, required=true)
    List<String> reg;

    @Parameter(names = {"-p", "--port" }, description="Port to listen on")
    Integer port = 8000;

    @Parameter(names = {"-t", "--threads" }, description="Size of server thread pool")
    Integer nThreads = NanoServer.DEFAULT_NUM_THREADS;

    @Override
    protected void doCommand(JeoCLI cli) throws Exception {
        ConsoleReader console = cli.getConsole();
        console.println("Starting NanoHTTPD on port " + port);
        console.println("Serving data from " + reg);
        console.flush();

        File f = new File(reg.get(0));
        DataRepository registry = registry(f, cli);
        
        NanoServer server = new NanoServer(port, null, nThreads, registry, null);
        server.join();

    }

    DataRepository registry(File f, JeoCLI cli) throws IOException {
        if (f.isDirectory()) {
            return new DirectoryRepository(f);
        }

        return new JSONRepository(f);
    }
}
