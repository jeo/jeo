package org.jeo.data;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.List;

public class DirectoryRegistry implements Registry {

    File baseDir;
    List<String> exts;

    public DirectoryRegistry(File baseDir, String... exts) {
        this.baseDir = baseDir;
        this.exts = exts.length > 0 ? Arrays.asList(exts) : null; 
    }

    @Override
    public Workspace get(final String key) {
        if (exts == null) {
            //search for any file with this base name
            String[] files = baseDir.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.startsWith(key+".");
                }
            });
            for (String file : files) {
                Workspace ws = Workspaces.create(new File(baseDir, file));
                if (ws != null) {
                    return ws;
                }
            }
        }
        else {
            //look for one of our specific extensions
            for (String ext : exts) {
                File f = new File(baseDir, key + "." + ext);
                if (f.exists()) {
                    Workspace ws = Workspaces.create(f);
                    if (ws != null) {
                        return ws;
                    }
                }
            }
        }

        return null;
    }

    @Override
    public void dispose() {

    }
}
