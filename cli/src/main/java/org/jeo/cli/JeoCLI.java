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
package org.jeo.cli;

import com.beust.jcommander.JCommander;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;
import jline.console.ConsoleReader;
import org.jeo.cli.cmd.DriversCmd;
import org.jeo.cli.cmd.InfoCmd;
import org.jeo.cli.cmd.JeoCmd;
import org.jeo.cli.cmd.QueryCmd;
import org.jeo.cli.cmd.RenderCmd;
import org.jeo.cli.cmd.RootCmd;
import org.jeo.cli.cmd.ServeCmd;
import org.jeo.cli.conv.JeoCLIConverterFactory;
import org.jeo.json.JeoJSONWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.util.Set;

public class JeoCLI {

    /**
     * input / output
     */
    CLIConsoleReader console;

    /**
     * root command
     */
    RootCmd root;

    /**
     * the commander
     */
    JCommander cmdr;

    /**
     * flag to control whether cli throws back errors
     */
    boolean throwErrors = false;

    /**
     * Runs the cli.
     */
    public static void main(String[] args) throws Exception {
        JeoCLI cli = new JeoCLI(System.in, System.out);
        cli.handle(args);
    }

    /**
     * Creates a new cli instance with the specified input / output streams.
     */
    public JeoCLI(InputStream in, OutputStream out) {
        try {
            console = new CLIConsoleReader(in, out);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }

        // needed for CTRL+C not to let the console broken
        console.getTerminal().setEchoEnabled(true);
        this.cmdr = initJCommander();
    }

    /**
     * Sets flag controlling whether cli should throw back errors.
     * <p>
     *  Default value is false, this method may be set to true for cases where
     *  the cli is being embedded.
     * </p>
     */
    public JeoCLI throwErrors(boolean throwErrors) {
        this.throwErrors = throwErrors;
        return this;
    }

    /**
     * Flag controlling whether cli should throw back errors.
     */
    public boolean throwErrors() {
        return throwErrors;
    }

    /**
     * The console reader containing reference to input/output streams.
     */
    public ConsoleReader console() {
        return console;
    }

    /**
     * Returns a print stream around <tt>console().getOutput()</tt>.
     */
    public PrintStream stream() {
        final Writer w = console.getOutput();
        return new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                w.write(b);
            }
            @Override
            public void flush() throws IOException {
                super.flush();
                w.flush();
            }

            @Override
            public void close() throws IOException {
                super.close();
                w.close();
            }
        });
    }

    /**
     * Raw output stream.
     * <p>
     * For writing text based content <tt>console().getOutput()</tt> should be used.
     * Commands that need to output binary content should utilize this stream.
     * </p>
     */
    public OutputStream output() {
        return console.out;
    }

    /**
     * Returns a new JSON writer connected to the console output stream.
     */
    public JeoJSONWriter newJSONWriter() {
        return new JeoJSONWriter(console().getOutput(), 2);
    }

    /**
     * Returns a new progress object for displaying a progress bar on the console.
     */
    public ConsoleProgress progress(int total) {
        return new ConsoleProgress(console(), total);
    }

    JCommander initJCommander() {
        root = new RootCmd();
        JCommander jcmdr = new JCommander(root);
        jcmdr.addConverterFactory(new JeoCLIConverterFactory());

        jcmdr.addCommand("drivers", new DriversCmd());
        jcmdr.addCommand("info", new InfoCmd());
        jcmdr.addCommand("query", new QueryCmd());
        jcmdr.addCommand("serve", new ServeCmd());
        jcmdr.addCommand("render", new RenderCmd());
        return jcmdr;
    }

    void handle(String... args) throws Exception {
        if (args == null || args.length == 0) {
            usage();
            return;
        }
        
        try {
            cmdr.parse(args);

            JCommander subcmdr = cmdr.getCommands().get(cmdr.getParsedCommand());

            JeoCmd cmd = subcmdr != null ? (JeoCmd) subcmdr.getObjects().get(0) : root;
            cmd.exec(this);
        }
        catch(Exception e) {
            if (throwErrors) {
                throw e;
            }

            if (e.getMessage() != null) {
                console.println(e.getMessage());
            }
            //console.flush();
        }
    }

    /**
     * Displays cli usage.
     */
    public void usage() {
        Set<String> commands = cmdr.getCommands().keySet();
        int maxLength = new Ordering<String>() {
            public int compare(String left, String right) {
                return Ints.compare(left.length(), right.length());
            };
        }.max(commands).length();

        try {
            console.println("usage: jeo <command> [<args>]");
            console.println();
            console.println("Commands:");
            console.println();
            for (String cmd : commands) {
                console.print("\t");
                console.print(Strings.padEnd(cmd, maxLength, ' '));
                console.print("\t");
                console.println(cmdr.getCommandDescription(cmd));
            }
            console.println();
            console.println("For detailed help on a specific command use jeo <command> -h");
            console.flush();
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     * Subclass used to maintain reference to output stream.
     */
    static class CLIConsoleReader extends ConsoleReader {

        OutputStream out;
        public CLIConsoleReader(InputStream in, OutputStream out) throws IOException {
            super(in, out);
            this.out = out;
        }
    }
}
