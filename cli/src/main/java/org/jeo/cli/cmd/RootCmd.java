package org.jeo.cli.cmd;

import org.jeo.JEO;
import org.jeo.cli.JeoCLI;

import com.beust.jcommander.Parameter;

public class RootCmd extends JeoCmd {

    @Parameter(names={"-v", "--version"}, description="Prints version info", help=true)
    boolean version;

    @Override
    protected void doCommand(JeoCLI cli) throws Exception {
        if (version) {
            JEO.printVersionInfo(cli.stream());
        }
    }

    @Override
    public void usage(JeoCLI cli) {
        cli.usage();
    }
}
