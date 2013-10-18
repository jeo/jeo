package org.jeo.nano;

import org.jeo.nano.NanoHTTPD.Response;

public class AppsHandler extends Handler {

    @Override
    public boolean canHandle(Request request, NanoJeoServer server) {
        String uri = request.getUri(); 
        return uri.startsWith("/apps") || uri.startsWith("/ol");
    }

    @Override
    public Response handle(Request request, NanoJeoServer server) throws Exception {
        return server.serveFile(request.getUri(), request.getHeader(), server.getWWWRoot(), true);
    }

}
