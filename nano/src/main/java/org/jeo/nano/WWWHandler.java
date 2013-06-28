package org.jeo.nano;

import org.jeo.nano.NanoHTTPD.Response;

public class WWWHandler extends Handler {

    @Override
    public boolean canHandle(Request request, NanoJeoServer server) {
        return request.getUri().startsWith("/www");
    }
    
    @Override
    public Response handle(Request request, NanoJeoServer server) {
        String uri = request.getUri().replaceAll("/www", "");
        return server.serveFile(uri, request.getHeader(), server.getWWWRoot(), true);
    }

}
