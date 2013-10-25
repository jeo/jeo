package org.jeo.nano;

import org.jeo.nano.NanoHTTPD.Response;

public class PingHandler extends Handler {

    @Override
    public boolean canHandle(Request request, NanoServer server) {
        return "/ping".equals(request.getUri());
    }

    @Override
    public Response handle(Request request, NanoServer server) throws Exception {
        return new Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "");
    }

}
