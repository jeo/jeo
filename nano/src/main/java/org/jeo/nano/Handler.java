package org.jeo.nano;

import org.jeo.nano.NanoHTTPD.Response;

public abstract class Handler {

    public abstract boolean canHandle(Request request);

    public abstract Response handle(Request request);
}
