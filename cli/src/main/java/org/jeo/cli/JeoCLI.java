package org.jeo.cli;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.util.Set;

import jline.console.ConsoleReader;

import org.jeo.cli.cmd.ConvertCmd;
import org.jeo.cli.cmd.DriversCmd;
import org.jeo.cli.cmd.InfoCmd;
import org.jeo.cli.cmd.JeoCmd;
import org.jeo.cli.cmd.QueryCmd;
import org.jeo.cli.cmd.RootCmd;
import org.jeo.cli.conv.JeoCLIConverterFactory;

import com.beust.jcommander.JCommander;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;

public class JeoCLI {

    ConsoleReader console;

    RootCmd root;

    JCommander cmdr;

    public static void main(String[] args) throws Exception {
        JeoCLI cli = new JeoCLI(createConsoleReader());
        cli.handle(args);
    }

    static ConsoleReader createConsoleReader() {
        try {
            ConsoleReader reader = new ConsoleReader(System.in, System.out);
            // needed for CTRL+C not to let the console broken
            reader.getTerminal().setEchoEnabled(true);
            return reader;
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public JeoCLI(ConsoleReader console) {
        this.console = console;
        this.cmdr = initJCommander();
    }

    public ConsoleReader getConsole() {
        return console;
    }

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
    
    JCommander initJCommander() {
        root = new RootCmd();
        JCommander jcmdr = new JCommander(root);
        jcmdr.addConverterFactory(new JeoCLIConverterFactory());
        jcmdr.addCommand("drivers", new DriversCmd());
        jcmdr.addCommand("query", new QueryCmd());
        jcmdr.addCommand("info", new InfoCmd());
        jcmdr.addCommand("convert", new ConvertCmd());
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
            cmd.run(this);
        }
        catch(Exception e) {
            if (e.getMessage() != null) {
                console.println(e.getMessage());
            }
            console.flush();
        }
    }

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
}
