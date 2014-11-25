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
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jeo.cli.JeoCLI;
import org.jeo.data.Disposable;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

public abstract class JeoCmd {

    @Parameter(names={"-h", "--help"}, description="Provides help for this command", help=true)
    boolean help;

    @Parameter(names={"-x", "--debug"}, description="Runs command in debug mode", help=true)
    boolean debug;
    
    Deque<Disposable> toDispose = new ArrayDeque<Disposable>();

    public final void run(JeoCLI cli) throws Exception {
        if (help) {
            usage(cli);
            return;
        }

        if (debug) {
            setUpDebugLogging();
        }
        try {
            doCommand(cli);
        }
        catch(Exception e) {
            if (debug) {
                print(e, cli);
            }
            else {
                cli.getConsole().println(e.getMessage());
            }
        }
        finally {
            while(!toDispose.isEmpty()) {
                Disposable d = toDispose.pop();
                try {
                    d.close();
                }
                catch(Exception e) {
                    //TODO: log this
                }
            }
        }
        cli.getConsole().flush();
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

    protected abstract void doCommand(JeoCLI cli) throws Exception;

    public void usage(JeoCLI cli) {
        JCommander jc = new JCommander(this);
        String cmd = this.getClass().getAnnotation(Parameters.class).commandNames()[0];
        jc.setProgramName("jeo " + cmd);
        jc.usage();
    }

    protected URI parseDataURI(String str) {
        try {
            URI uri = new URI(str);
            if (uri.getScheme() == null) {
                //assume a file based uri
                URI tmp = new File(uri.getPath()).toURI();
                uri = uri.getFragment() != null ? 
                    new URI(tmp.getScheme(), null, tmp.getPath(), uri.getFragment()) : tmp;
            }
            return uri;
        }
        catch(URISyntaxException e) {
            throw new IllegalArgumentException("Invalid data source uri: " + str);
        }
    }

    protected <T extends Disposable> T open(T obj) {
        if (obj != null) {
            toDispose.push(obj);
        }
        return obj;
    }

    protected void print(Exception e, JeoCLI cli) {
        e.printStackTrace(new PrintWriter(cli.getConsole().getOutput()));
    }
}
