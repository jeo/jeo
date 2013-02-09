package org.jeo.nano;

import java.io.File;

import org.jeo.nano.NanoHTTPD.Response;

public class WWWHandler extends Handler {

    NanoHTTPD server;
    File wwwRoot;

    WWWHandler(NanoHTTPD server, File wwwRoot) {
        this.server = server;
        this.wwwRoot = wwwRoot;
    }

    @Override
    public boolean canHandle(Request request) {
        return request.getUri().startsWith("/www");
    }
    
    @Override
    public Response handle(Request request) {
        String uri = request.getUri().replaceAll("/www", "");
        return server.serveFile(uri, request.getHeader(), wwwRoot, false);
    }

}
