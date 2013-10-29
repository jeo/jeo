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

import org.jeo.data.Registry;
import org.jeo.data.SimpleRegistry;
import org.jeo.nano.NanoHTTPD.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NanoServer extends NanoHTTPD {

    public static final int DEFAULT_NUM_THREADS = 25;

    static final Logger LOG = LoggerFactory.getLogger(NanoServer.class);

    Registry reg;
    List<Handler> handlers;

    public NanoServer(int port, File wwwRoot, int nThreads, Registry reg, List<Handler> handlers) 
        throws IOException {
        this(port, wwwRoot, nThreads, reg, 
            handlers != null ? handlers.toArray(new Handler[handlers.size()]) : new Handler[]{});
    }

    public NanoServer(int port, File wwwRoot, int nThreads, Registry reg, Handler... handlers) 
        throws IOException {
        super(port, wwwRoot, nThreads);

        this.reg = reg;

        this.handlers = new ArrayList<Handler>();
        this.handlers.add(new RootHandler());

        if (handlers == null || handlers.length == 0) {
            handlers = new Handler[]{new TileHandler(), new FeatureHandler()};
        }

        this.handlers.addAll(Arrays.asList(handlers));
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

    public Registry getRegistry() {
        return reg;
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
        if (uri.toLowerCase().startsWith("/ping")) {
            return new Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "");
        }

        LOG.debug(method + " " + uri + "?" + parms);

        Request request = new Request(uri, method, header, parms, files);
         
        //find the handler for this request
        Handler h = findHandler(request);
        if (h == null) {
            return new Response(HTTP_NOTFOUND, MIME_PLAINTEXT, "No handler for request");
        }

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
                new AppsHandler(new File("/Users/jdeolive/Projects/jeo/apps")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        try { System.in.read(); } catch( Throwable t ) {}
    }

    static Registry loadRegistry() {
        return new SimpleRegistry();
    }

    static void usage() {
        System.out.println(NanoServer.class.getCanonicalName() + " <port> [<wwwRoot>]");
        System.exit(1);
    }
}
