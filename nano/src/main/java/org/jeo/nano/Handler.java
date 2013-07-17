package org.jeo.nano;

import static org.jeo.nano.NanoHTTPD.HTTP_NOTFOUND;

import java.io.IOException;

import org.jeo.data.Dataset;
import org.jeo.data.Registry;
import org.jeo.data.Workspace;
import org.jeo.nano.NanoHTTPD.Response;

public abstract class Handler {

    public abstract boolean canHandle(Request request, NanoJeoServer server);

    public abstract Response handle(Request request, NanoJeoServer server) throws Exception;

    protected Workspace findWorkspace(String key, Registry reg) throws IOException {
        Object obj = reg.get(key);
        if (obj == null || !(obj instanceof Workspace)) {
            //no such layer
            throw new HttpException(HTTP_NOTFOUND, "No such workspace: " + key);
        }
        return (Workspace) obj;
    }

    protected Dataset findDataset(String key, Registry reg) throws IOException {
        Object obj = null;

        String[] split = key.split("/");
        if (split.length == 1) {
            obj = reg.get(split[0]);
        }
        else {
            Workspace ws = findWorkspace(split[0], reg);
            obj = ws.get(split[1]);
        }

        if (obj == null || !(obj instanceof Dataset)) {
            //no such layer
            throw new HttpException(HTTP_NOTFOUND, "No such layer: " + key);
        }

        return (Dataset)obj;
    }
}
