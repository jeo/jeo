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
package org.jeo.nano;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import org.jeo.data.DataRepository;
import org.jeo.data.DataRepositoryView;
import org.jeo.data.DirectoryRepository;
import org.jeo.data.mem.MemRepository;
import org.jeo.map.render.RendererRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NanoServer extends NanoHTTPD {

    public static final int DEFAULT_NUM_THREADS = 25;

    static final Logger LOG = LoggerFactory.getLogger(NanoServer.class);

    DataRepositoryView reg;
    RendererRegistry rendererRegistry;
    List<Handler> handlers;

    public NanoServer(int port, File wwwRoot, int nThreads, DataRepositoryView reg, List<Handler> handlers)
        throws IOException {
        this(port, wwwRoot, nThreads, reg, handlers, null);
    }
    
    public NanoServer(int port, File wwwRoot, int nThreads, DataRepositoryView reg, List<Handler> handlers,
        RendererRegistry rendererRegistry) throws IOException {
        super(port, wwwRoot, nThreads);

        this.reg = reg;
        this.rendererRegistry = rendererRegistry;

        this.handlers = new ArrayList<Handler>();
        this.handlers.add(new DataHandler());

        if (handlers == null || handlers.isEmpty()) {
            handlers = Arrays.asList(new TileHandler(), new FeatureHandler(), new StyleHandler(),
                    new WMSHandler(), new WMTSHandler());
        }

        this.handlers.addAll(handlers);
        if (wwwRoot != null) {
            boolean addAppHandler = true;
            for (Handler h : handlers) {
                if (h instanceof AppsHandler) {
                    addAppHandler = false;
                    break;
                }
            }

            if (addAppHandler) {
                this.handlers.add(new AppsHandler());
            }
        }

        for (Handler h : this.handlers) {
            h.init(this);
        }
    }

    @Override
    protected void error(String message, Throwable t) {
        LOG.error(message, t);
    }

    public DataRepositoryView getRegistry() {
        return reg;
    }

    public RendererRegistry getRendererRegistry() {
        return rendererRegistry;
    }

    public void setRendererRegistry(RendererRegistry rendererRegistry) {
        this.rendererRegistry = rendererRegistry;
    }

    public File getWWWRoot() {
        return getRootDir();
    }

    @Override
    public Response serve(String uri, String method, Properties header, Properties parms, 
        Properties files) {

        if (uri == null) {
            uri = "";
        }

        // handle a "ping"
        if (uri.equals("/ping")) {
            return new Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "");
        }

        Request request = new Request(uri, method, header, parms, files);
         
        //find the handler for this request
        Handler h = findHandler(request);
        if (h == null) {
            return new Response(HTTP_NOTFOUND, MIME_PLAINTEXT, "No handler for request");
        }

        LOG.debug(method + " " + uri + "?" + parms);
        try {
            return h.handle(request, this);
        }
        catch(HttpException e) {
            return e.toResponse();
        }
        catch(Exception e) {
            LOG.warn("Request threw exception", e);
            return new Response(HTTP_INTERNALERROR, MIME_PLAINTEXT, toStream(e));
        }
    }

    InputStream toStream(Exception e) {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();

        PrintStream stream = new PrintStream(bout);
        e.printStackTrace(stream);
        stream.flush();

        return new ByteArrayInputStream(bout.toByteArray());
    }

    Handler findHandler(Request request) {
        for (Handler h : handlers) {
            if (h.canHandle(request, this)) {
                return h;
            }
        }
        return null;
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            usage();
        }

        Opts opts = parseOpts(args);

        if (opts.verbose) {
            Level level = Level.ALL;
            java.util.logging.Logger.getLogger("").setLevel(level);
            java.util.logging.Logger.getLogger("").getHandlers()[0].setLevel(level);
        }

        // make number of threads configurable
        try {
            new NanoServer(opts.port, opts.root, DEFAULT_NUM_THREADS, loadRegistry(opts), 
                null, null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        try { System.in.read(); } catch( Throwable t ) {}
    }

    static Opts parseOpts(String[] args) {
        List<String> argList = new LinkedList(Arrays.asList(args));
        Opts opts = new Opts();

        opts.verbose = argList.remove("-v");

        if (argList.size() % 2 != 0) {
            usage();
        }

        for (Iterator<String> a = argList.iterator(); a.hasNext();) {
            String arg = a.next();
            if ("-p".equalsIgnoreCase(arg)) {
                opts.port = Integer.parseInt(a.next());
            }
            else if ("-r".equalsIgnoreCase(arg)) {
                opts.root = new File(a.next());
            }
            else if ("-d".equalsIgnoreCase(arg)) {
                opts.data = new File(a.next());
            }
            else {
                usage();
            }
        }
        return opts;
    }

    static DataRepositoryView loadRegistry(Opts opts) {
        DataRepository repo;
        if (opts.data != null) {
            repo = new DirectoryRepository(opts.data);
        } else {
            repo = new MemRepository();
        }
        return new DataRepositoryView(repo);
    }

    static void usage() {
        System.out.println(NanoServer.class.getCanonicalName() + "[-p <port>] [-r <root>] [-d <dataRoot>]");
        System.exit(1);
    }

    static class Opts {
        Integer port = 8000;
        File root = null;
        File data = null;
        boolean verbose = false;
    }
}
