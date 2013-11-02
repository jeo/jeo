package org.jeo.nano;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.jeo.data.DataRepository;
import org.jeo.data.mem.MemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NanoServer extends NanoHTTPD {

    public static final int DEFAULT_NUM_THREADS = 25;

    static final Logger LOG = LoggerFactory.getLogger(NanoServer.class);

    DataRepository reg;
    List<Handler> handlers;

    MapRenderer renderer;

    public NanoServer(int port, File wwwRoot, int nThreads, DataRepository reg, List<Handler> handlers) 
        throws IOException {
        this(port, wwwRoot, nThreads, reg, handlers, null);
    }
    
    public NanoServer(int port, File wwwRoot, int nThreads, DataRepository reg, List<Handler> handlers, 
        MapRenderer renderer) throws IOException {
        super(port, wwwRoot, nThreads);

        this.reg = reg;
        this.renderer = renderer;

        this.handlers = new ArrayList<Handler>();
        this.handlers.add(new DataHandler());

        if (handlers == null || handlers.isEmpty()) {
            handlers = Arrays.asList(new TileHandler(), new FeatureHandler(), new StyleHandler());
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

        for (Handler h : handlers) {
            h.init(this);
        }
    }

    public DataRepository getRegistry() {
        return reg;
    }

    public File getWWWRoot() {
        return getRootDir();
    }

    public MapRenderer getRenderer() {
        return renderer;
    }

    public void setRenderer(MapRenderer renderer) {
        this.renderer = renderer;
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

        Integer port = Integer.parseInt(args[0]);
        File wwwRoot = null;
        if (args.length > 1) {
            wwwRoot = new File(args[1]);
        }

        // make number of threads configurable
        try {
            new NanoServer(port, wwwRoot, DEFAULT_NUM_THREADS, loadRegistry(),
                null, null);
                /*(List)Arrays.asList(new AppsHandler(new File("/Users/jdeolive/Projects/jeo/apps")))
                new MapRenderer() {
                    @Override
                    public void render(Map map, OutputStream output) throws IOException {
                        Java2D.render(map, output);
                    }
                });*/
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        try { System.in.read(); } catch( Throwable t ) {}
    }

    static DataRepository loadRegistry() {
        //return new org.jeo.data.DirectoryRegistry(new File("/Users/jdeolive/Documents/GeoData"));
        return new MemRepository();
    }

    static void usage() {
        System.out.println(NanoServer.class.getCanonicalName() + " <port> [<wwwRoot>]");
        System.exit(1);
    }
}
