package org.jeo.nano;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jeo.data.Dataset;
import org.jeo.data.DataRepository;
import org.jeo.data.Workspace;
import static org.jeo.nano.NanoHTTPD.HTTP_BADREQUEST;
import static org.jeo.nano.NanoHTTPD.HTTP_NOTFOUND;
import org.jeo.nano.NanoHTTPD.Response;
import org.jeo.proj.Proj;
import org.jeo.util.Pair;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.osgeo.proj4j.UnknownAuthorityCodeException;
import org.osgeo.proj4j.UnsupportedParameterException;

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

    protected CoordinateReferenceSystem parseCRS(Properties props) {
        return parseCRS(props.getProperty("srs"));
    }

    protected CoordinateReferenceSystem parseCRS(String srs) {
        try {
            return srs == null ? null : Proj.crs(srs);
        } catch (UnsupportedParameterException upe) {
            throw new HttpException(HTTP_BADREQUEST, "Cannot locate provided srs: " + srs);
        } catch (UnknownAuthorityCodeException uace) {
            throw new HttpException(HTTP_BADREQUEST, "Cannot locate provided authority: " + srs);
        } catch (IllegalStateException iae) {
            throw new HttpException(HTTP_BADREQUEST, "Cannot locate provided srs: " + srs);
        }
    }

    protected String createPath(Request request) {
        Matcher m = (Matcher) request.getContext().get(Matcher.class);
        String second = m.group(2);
        return m.group(1) + (second == null ? "" : "/" + second);
    }

    protected Workspace findWorkspace(String key, DataRepository reg) throws IOException {
        Object obj = reg.get(key);
        if (obj == null || !(obj instanceof Workspace)) {
            //no such layer
            throw new HttpException(HTTP_NOTFOUND, "No such workspace: " + key);
        }
        return (Workspace) obj;
    }

    protected Pair<Dataset, Workspace> findDataset(Request request, DataRepository reg) throws IOException {
        Pair<Object, Object> found = findObject(request, reg);
        if (found == null || !(found.first() instanceof Dataset)) {
            throw new HttpException(HTTP_NOTFOUND, "No such dataset at: " + request.getUri());
        }
        return Pair.of((Dataset) found.first(), (Workspace) found.second());
    }

    protected Pair<Object,Object> findObject(Request request, DataRepository reg) throws IOException {
        Matcher m = (Matcher) request.getContext().get(Matcher.class);
        String first = m.group(1);
        if (first != null) {
            String second = m.group(2);
            if (second != null) {
                try {
                    Workspace ws = (Workspace) reg.get(first);
                    if (ws == null) {
                        throw new HttpException(HTTP_NOTFOUND, "no such workspace: " + first);
                    }

                    Dataset ds = ws.get(second);
                    if (ds == null) {
                        throw new HttpException(HTTP_NOTFOUND,
                            "no such dataset: " + second + " in workspace: " + first);
                    }

                    return new Pair<Object,Object>(ds, ws);
                } catch (ClassCastException e) {
                    throw new HttpException(HTTP_BADREQUEST, first + " is not a workspace");
                }
            }
            else {
                return Pair.of((Object)reg.get(first), null);
            }
        }
        else {
            return null;
        }
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
