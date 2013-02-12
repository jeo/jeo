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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NanoJeoServer extends NanoHTTPD {

    static final Logger LOG = LoggerFactory.getLogger(NanoJeoServer.class);

    Registry reg;
    List<Handler> handlers;

    public NanoJeoServer(int port, File wwwRoot, Registry reg, Handler... handlers) 
        throws IOException {
        super(port, wwwRoot);

        this.reg = reg;

        if (handlers == null || handlers.length == 0) {
            handlers = new Handler[]{new TileHandler(reg), new FeatureHandler(reg)};
        }

        this.handlers = new ArrayList<Handler>(Arrays.asList(handlers));
        if (wwwRoot != null) {
            this.handlers.add(new WWWHandler(this, wwwRoot));
        }
    }

    @Override
    public Response serve(String uri, String method, Properties header, Properties parms, 
        Properties files) {

        LOG.debug(method + " " + uri + "?" + parms);

        Request request = new Request(uri, method, header, parms, files);
         
        //find the handler for this request
        Handler h = findHandler(request);
        if (h == null) {
            return new Response(HTTP_NOTFOUND, MIME_PLAINTEXT, "No handler for request");
        }

        try {
            return h.handle(request);
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
            if (h.canHandle(request)) {
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

        SimpleRegistry reg = new SimpleRegistry();

//        org.jeo.geopkg.GeoPackage states, ne;
//        try {
//            states = new org.jeo.geopkg.GeoPackage(new File("/Users/jdeolive/states2.db"));
//            ne = new org.jeo.geopkg.GeoPackage(new File("/Users/jdeolive/ne.db"));
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//
//        reg.put("states", states);
//        reg.put("ne", ne);

        try {
            new NanoJeoServer(port, wwwRoot, reg);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        try { System.in.read(); } catch( Throwable t ) {}
    }

    static void usage() {
        System.out.println(NanoJeoServer.class.getCanonicalName() + " <port> [<wwwRoot>]");
        System.exit(1);
    }
}
