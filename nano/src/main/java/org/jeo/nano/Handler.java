package org.jeo.nano;

import static org.jeo.nano.NanoHTTPD.HTTP_NOTFOUND;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jeo.data.Dataset;
import org.jeo.data.Registry;
import org.jeo.data.Workspace;
import org.jeo.nano.NanoHTTPD.Response;
import org.jeo.util.Pair;

public abstract class Handler {

    public void init(NanoServer server) {
    }

    public abstract boolean canHandle(Request request, NanoServer server);

    public abstract Response handle(Request request, NanoServer server) throws Exception;

    protected boolean match(Request request, Pattern pattern) {
        Matcher m = pattern.matcher(request.getUri());
        if (m.matches()) {
            //save the matcher
            request.getContext().put(Matcher.class, m);
            return true;
        }
        return false;
    }

    protected Workspace findWorkspace(String key, Registry reg) throws IOException {
        Object obj = reg.get(key);
        if (obj == null || !(obj instanceof Workspace)) {
            //no such layer
            throw new HttpException(HTTP_NOTFOUND, "No such workspace: " + key);
        }
        return (Workspace) obj;
    }

    protected Pair<Dataset, Workspace> findDataset(String key, Registry reg) throws IOException {
        Workspace ws = null;
        Object obj = null;

        String[] split = key.split("/");
        if (split.length == 1) {
            obj = reg.get(split[0]);
        }
        else {
            ws = findWorkspace(split[0], reg);
            obj = ws.get(split[1]);
        }

        if (obj == null || !(obj instanceof Dataset)) {
            //no such layer
            throw new HttpException(HTTP_NOTFOUND, "No such layer: " + key);
        }

        return Pair.of((Dataset)obj, ws);
    }

    protected String renderTemplate(String template, Map<String,String> vars) throws IOException {
        BufferedReader in = 
            new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(template)));
        try {
            StringBuilder sb = new StringBuilder();

            String line = null;
            while((line = in.readLine()) != null) {
                for (Map.Entry<String,String> e: vars.entrySet()) {
                    line = line.replace("%" + e.getKey() + "%", e.getValue());
                }
                sb.append(line).append("\n");
            }
            return sb.toString();
        }
        finally {
            in.close();
        }
    }
}
