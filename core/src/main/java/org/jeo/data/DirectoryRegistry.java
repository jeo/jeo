package org.jeo.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import org.jeo.data.mem.MemWorkspace;
import org.jeo.util.Util;

/**
 * A registry that loads workspaces from files in a specified directory.
 *  
 * @author Justin Deoliveira, OpenGeo
 */
public class DirectoryRegistry implements Registry {

    /** base directory */
    File baseDir;

    /** driver registry */
    DriverRegistry drivers;

    /** list of file extensions to restrict to */
    List<String> exts;

    /**
     * Constructs a new registry.
     * 
     * @param baseDir The directory to search for files in.
     * @param exts Optional file name extensions to restrict look ups to.
     */
    public DirectoryRegistry(File baseDir, String... exts) {
         this(baseDir, Drivers.REGISTRY, exts);
    }

    /**
     * Constructs a new registry.
     * 
     * @param baseDir The directory to search for files in.
     * @param exts Optional file name extensions to restrict look ups to.
     */
    public DirectoryRegistry(File baseDir, DriverRegistry drivers, String... exts) {
        if (baseDir == null) {
            throw new NullPointerException("baseDir must not be null");
        }
        if (!baseDir.exists()) {
            throw (IllegalArgumentException) new IllegalArgumentException()
                .initCause(new FileNotFoundException(baseDir.getPath()));
        }

        this.baseDir = baseDir;
        this.drivers = drivers;
        this.exts = exts.length > 0 ? Arrays.asList(exts) : null; 
    }
    
    @Override
    public Iterable<DataRef<?>> list() throws IOException {
        // list all files, possibily filtering by extension
        String[] files = exts == null ? baseDir.list() : baseDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return exts.contains(Util.extension(name));
            }
        });

        // process files to see what ones we have drivers for
        LinkedHashSet<DataRef<?>> items = new LinkedHashSet<DataRef<?>>();
        for (String fn : files) {
            Driver<?> drv = Drivers.find(new File(baseDir, fn).toURI(), drivers);
            if (drv != null) {
                items.add(new DataRef(Util.base(fn), drv));
            }
        }

        return items;
    }

    @Override
    public Object get(final String key) throws IOException {
        if (exts == null) {
            //search for any file with this base name
            String[] files = baseDir.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.startsWith(key+".");
                }
            });
            for (String file : files) {
                Object obj = Drivers.open(new File(baseDir, file), drivers);
                if (obj != null) {
                    return obj;
                }
            }
        }
        else {
            //look for one of our specific extensions
            for (String ext : exts) {
                File f = new File(baseDir, key + "." + ext);
                if (f.exists()) {
                    return Drivers.open(f, drivers);
                }
            }
        }

        return null;
    }

    Workspace workspace(File file) throws IOException {
        Object obj = Drivers.open(file);
        if (obj instanceof Workspace) {
            return (Workspace) obj;
        }
        else if (obj instanceof Dataset) {
            Dataset data = (Dataset) obj;
            MemWorkspace mem = new MemWorkspace();
            mem.put(data.getName(), data);
            return mem;
        }
        else {
            return null;
        }
    }

    public void close() {
    }
}
