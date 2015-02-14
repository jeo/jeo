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
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jeo.cli.JeoCLI;
import org.jeo.data.Dataset;
import org.jeo.data.Disposable;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.jeo.data.Drivers;
import org.jeo.data.Workspace;
import org.jeo.util.Disposer;
import org.jeo.util.Optional;
import org.jeo.util.Pair;

import static java.lang.String.format;

public abstract class JeoCmd {

    @Parameter(names={"-h", "--help"}, description="Provides help for this command", help=true)
    boolean help;

    @Parameter(names={"-x", "--debug"}, description="Runs command in debug mode", help=true)
    boolean debug;
    
    Disposer disposer = new Disposer();

    public final void exec(JeoCLI cli) throws Exception {
        if (help) {
            usage(cli);
            return;
        }

        if (debug) {
            setUpDebugLogging();
        }
        try {
            run(cli);
        }
        catch(Exception e) {
            if (cli.throwErrors()) {
                throw e;
            }
            if (debug) {
                print(e, cli);
            }
            else {
                cli.console().println(e.getMessage());
            }
        }
        finally {
            disposer.close();
        }
        cli.console().flush();
    }
    
    void setUpDebugLogging() {
        // check for jdk logging property, if present don't do anything
        if (System.getProperty("java.util.logging.config.file") == null) {
            Logger log = Logger.getLogger("org.jeo");
            log.setLevel(Level.ALL);

            ConsoleHandler handler = new ConsoleHandler();
            handler.setLevel(Level.ALL);
            log.addHandler(handler);
        }
    }

    protected abstract void run(JeoCLI cli) throws Exception;

    public void usage(JeoCLI cli) {
        JCommander jc = new JCommander(this);
        String cmd = this.getClass().getAnnotation(Parameters.class).commandNames()[0];
        jc.setProgramName("jeo " + cmd);
        jc.usage();
    }

    protected <T extends Disposable> T open(T obj) {
        return disposer.open(obj);
    }

    protected void print(Exception e, JeoCLI cli) {
        e.printStackTrace(new PrintWriter(cli.console().getOutput()));
    }

    /**
     * Parses a data uri.
     * <p>
     * The fragment is stripped off the uri and returned separately.
     * </p>
     * @return A pair of (base uri, fragment)
     */
    public static Pair<URI,String> parseDataURI(String str) {
        try {
            URI uri = new URI(str);
            if (uri.getScheme() == null) {
                //assume a file based uri
                URI tmp = new File(uri.getPath()).toURI();
                uri = uri.getFragment() != null ?
                    new URI(tmp.getScheme(), null, tmp.getPath(), uri.getFragment()) : tmp;
            }

            // strip off fragment
            String frag = uri.getFragment();
            if (frag != null) {
                uri = new URI(
                    uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(), uri.getQuery(), null);
            }

            return Pair.of(uri, frag);
        }
        catch(URISyntaxException e) {
            throw new IllegalArgumentException("Invalid data source uri: " + str, e);
        }
    }

    /**
     * Opens a dataset from a data uri.
     */
    protected Optional<Dataset> openDataset(String ref) throws IOException {
        Pair<URI,String> uri = parseDataURI(ref);

        Dataset dataset;

        if (uri.second != null) {
            // reference through a workspace
            Workspace ws = open(Drivers.open(uri.first, Workspace.class));
            if (ws == null) {
                throw new IllegalArgumentException("Unable to open workspace: " + uri.first);
            }

            dataset = open(ws.get(uri.second));
            if (dataset == null) {
                throw new IllegalArgumentException(
                    format("No dataset named %s in workspace: %s", uri.second, uri.first));
            }
        }
        else {
            // straight dataset reference
            try {
                dataset = open((Dataset) Drivers.open(uri.first));
            }
            catch(ClassCastException e) {
                throw new IllegalArgumentException(uri.first + " is not a dataset");
            }
        }

        return Optional.of(dataset);
    }
}
