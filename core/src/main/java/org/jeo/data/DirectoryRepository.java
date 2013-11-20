package org.jeo.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import org.jeo.data.mem.MemWorkspace;
import org.jeo.filter.Filter;
import org.jeo.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A repository that loads workspaces from files in a specified directory.
 *  
 * @author Justin Deoliveira, OpenGeo
 */
public class DirectoryRepository implements DataRepository {

    static Logger LOG = LoggerFactory.getLogger(DirectoryRepository.class);

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
    public DirectoryRepository(File baseDir, String... exts) {
         this(baseDir, Drivers.REGISTRY, exts);
    }

    /**
     * Constructs a new registry.
     * 
     * @param baseDir The directory to search for files in.
     * @param exts Optional file name extensions to restrict look ups to.
     */
    public DirectoryRepository(File baseDir, DriverRegistry drivers, String... exts) {
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

    /**
     * The base directory of the repository.
     */
    public File getDirectory() {
        return baseDir;
    }

    @Override
    public Iterable<Handle<Object>> query(Filter<? super Handle<Object>> filter) 
        throws IOException {
        
        // list all files, possibily filtering by extension
        String[] files = exts == null ? baseDir.list() : baseDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return exts.contains(Util.extension(name));
            }
        });

        // process files to see what ones we have drivers for
        LinkedHashSet<Handle<Object>> items = new LinkedHashSet<Handle<Object>>();
        for (String fn : files) {
            Driver<?> drv = Drivers.find(new File(baseDir, fn).toURI(), drivers);
            if (drv != null) {
                String name = Util.base(fn);
                Handle<Object> h = new Handle<Object>(name, drv.getType(), drv) {
                    @Override
                    protected Object doResolve() throws IOException {
                        return get(name);
                    }
                };
                if (filter.apply(h)) {
                    items.add(h);
                }
            }
        }

        return items;
    }

    @Override
    public Workspace get(final String key) throws IOException {
        if (exts == null) {
            //search for any file with this base name
            String[] files = baseDir.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.startsWith(key+".");
                }
            });
            for (String file : files) {
                return newWorkspaceOrNull(new File(baseDir, file));
            }
        }
        else {
            //look for one of our specific extensions
            for (String ext : exts) {
                File f = new File(baseDir, key + "." + ext);
                if (f.exists()) {
                    return newWorkspaceOrNull(f);
                }
            }
        }

        return null;
    }

    Workspace newWorkspaceOrNull(File file) throws IOException {
        Object obj = Drivers.open(file, drivers);
        if (obj != null) {
            if (obj instanceof Workspace) {
                return (Workspace) obj;
            }
            else if (obj instanceof Dataset) {
                return new SingleWorkspace((Dataset)obj);
            }
            else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(
                        "object: " + obj + " not a workspace or dataset, file: " + file.getPath());
                }
            }
        }
        else {
            LOG.debug("Unable to open file: " + file.getPath());
        }
        
        return null;
    }

    public void close() {
    }
}
