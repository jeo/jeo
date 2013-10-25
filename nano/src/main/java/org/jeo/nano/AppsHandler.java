package org.jeo.nano;

import java.io.File;

import org.jeo.nano.NanoHTTPD.Response;

public class AppsHandler extends Handler {

    File appsDir = null;

    public AppsHandler() {
        this(null);
    }

    public AppsHandler(File appsDir) {
        this.appsDir = appsDir;
    }

    @Override
    public void init(NanoServer server) {
        if (appsDir == null) {
            if (server.getWWWRoot() == null) {
                throw new IllegalStateException(
                    "www root not set, unable to create relative apps directory");
            }

            appsDir = new File(server.getWWWRoot(), "apps");
            if (!appsDir.exists()) {
                if (!appsDir.mkdirs()) {
                    throw new IllegalStateException(
                        "unable to create apps directory: " + appsDir.getPath());
                }
            }
        }
    }

    @Override
    public boolean canHandle(Request request, NanoServer server) {
        String uri = request.getUri(); 
        return uri.startsWith("/apps") || uri.startsWith("/ol");
    }

    @Override
    public Response handle(Request request, NanoServer server) throws Exception {
        String uri = request.getUri();
        if (uri.startsWith("/apps")) {
            uri = uri.replaceFirst("/apps", "");
        }

        return server.serveFile(uri, request.getHeader(), appsDir, true);
    }

}
