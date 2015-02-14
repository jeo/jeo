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

import java.io.File;
import java.io.IOException;
import java.util.List;

import jline.console.ConsoleReader;

import org.jeo.cli.JeoCLI;
import org.jeo.data.DataRepository;
import org.jeo.data.DataRepositoryView;
import org.jeo.data.DirectoryRepository;
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
    protected void run(JeoCLI cli) throws Exception {
        ConsoleReader console = cli.console();
        console.println("Starting NanoHTTPD on port " + port);
        console.println("Serving data from " + reg);
        console.flush();

        File f = new File(reg.get(0));
        DataRepositoryView registry = registry(f, cli);
        
        NanoServer server = new NanoServer(port, null, nThreads, registry, null);
        server.join();

    }

    DataRepositoryView registry(File f, JeoCLI cli) throws IOException {
        DataRepository repo;
        if (f.isDirectory()) {
            repo = new DirectoryRepository(f);
        } else {
            repo = new JSONRepository(f);
        }
        return new DataRepositoryView(repo);
    }
}
