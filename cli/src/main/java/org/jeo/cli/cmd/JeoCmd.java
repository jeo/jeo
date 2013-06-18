package org.jeo.cli.cmd;

import java.io.File;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayDeque;
import java.util.Deque;

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
