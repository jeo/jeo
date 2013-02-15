package org.jeo.data;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

public class DirectoryRegistry implements Registry {

    File baseDir;
    List<String> exts;

    public DirectoryRegistry(File baseDir, String... exts) {
        this.baseDir = baseDir;
        this.exts = exts.length > 0 ? Arrays.asList(exts) : null; 
    }

    @Override
    public Iterator<String> keys() {
        LinkedHashSet<String> set = new LinkedHashSet<String>();

        if (exts == null) {
            for (String fn : baseDir.list()) {
                set.add(basename(fn));
            }
        }
        else {
            for (final String ext : exts) {
                String[] files = baseDir.list(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        int dot = name.lastIndexOf('.');
                        return ext.equalsIgnoreCase(name.substring(dot+1));
                    }
                });
                for (String file : files) {
                    set.add(basename(file));
                }
            }
        }

        return set.iterator();
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

    String basename(String fn) {
        int dot = fn.lastIndexOf('.');
        return fn.substring(0, dot);
    }
}
