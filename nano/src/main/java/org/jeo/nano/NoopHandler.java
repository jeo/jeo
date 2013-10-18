package org.jeo.nano;

import org.jeo.nano.NanoHTTPD.Response;

public class NoopHandler extends Handler {

    @Override
    public boolean canHandle(Request request, NanoServer server) {
        return false;
    }

    @Override
    public Response handle(Request request, NanoServer server) {
        return null;
    }

}
