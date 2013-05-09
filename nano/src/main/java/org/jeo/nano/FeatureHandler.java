package org.jeo.nano;

import static org.jeo.nano.NanoHTTPD.HTTP_BADREQUEST;
import static org.jeo.nano.NanoHTTPD.HTTP_CREATED;
import static org.jeo.nano.NanoHTTPD.HTTP_METHOD_NOT_ALLOWED;
import static org.jeo.nano.NanoHTTPD.HTTP_NOTFOUND;
import static org.jeo.nano.NanoHTTPD.HTTP_OK;
import static org.jeo.nano.NanoHTTPD.MIME_JSON;
import static org.jeo.nano.NanoHTTPD.MIME_PLAINTEXT;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jeo.data.Cursor;
import org.jeo.data.Dataset;
import org.jeo.data.Query;
import org.jeo.data.Registry;
import org.jeo.data.VectorData;
import org.jeo.data.Workspace;
import org.jeo.feature.Feature;
import org.jeo.feature.Features;
import org.jeo.feature.Schema;
import org.jeo.geojson.GeoJSONReader;
import org.jeo.geojson.GeoJSONWriter;
import org.jeo.nano.NanoHTTPD.Response;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;

public class FeatureHandler extends Handler {

    static final Logger LOG = LoggerFactory.getLogger(NanoJeoServer.class);

    // /features/<workspace>[/<layer>]
    static final Pattern FEATURES_URI_RE = 
        Pattern.compile("/features/([^/]+)(?:/([^/]+))?/?", Pattern.CASE_INSENSITIVE);

    Registry reg;

    public FeatureHandler(Registry reg) {
        this.reg = reg;
    }

    @Override
    public boolean canHandle(Request request) {
        Matcher m = FEATURES_URI_RE.matcher(request.getUri());
        if (m.matches()) {
            //save the matcher
            request.getContext().put(Matcher.class, m);
            return true;
        }
        return false;
    }

    @Override
    public Response handle(Request request) {
        try {
            if ("GET".equalsIgnoreCase(request.getMethod())) {
                return handleGet(request);
            }
            else if ("POST".equalsIgnoreCase(request.getMethod())) {
                return handlePost(request);
            }
    
            return new Response(HTTP_METHOD_NOT_ALLOWED, MIME_PLAINTEXT, "");
        }
        catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    Response handleGet(Request request) throws IOException {
        VectorData layer = findVectorLayer(request);

        //parse the bbox
        Properties q = request.getParms();
        if (!q.containsKey("bbox")) {
            return new Response(HTTP_BADREQUEST, MIME_PLAINTEXT, "Request must specify bbox");
        }

        Envelope bbox = parseBBOX(q.getProperty("bbox"));

        GeoJSONWriter w = new GeoJSONWriter();

        Cursor<Feature> c = layer.cursor(new Query().bounds(bbox));

        w.featureCollection();
        while(c.hasNext()) {
            w.feature(c.next());
        }
        w.endFeatureCollection();
        
        return new Response(HTTP_OK, MIME_JSON, w.toString());
    }

    Response handlePost(Request request) throws IOException {
        Matcher m = (Matcher) request.getContext().get(Matcher.class);

        String file = request.getFiles().getProperty("content");
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
        try {
            if (m.group(2) == null) {
                return handlePostCreateLayer(request, in);
            }
            else {
                return handlePostAddFeatures(request, in);
            }
        }
        finally {
            in.close();
        }
    }

    Response handlePostCreateLayer(Request request, InputStream body) throws IOException {
        Schema schema = (Schema) new GeoJSONReader().read(body);

        Workspace ws = findWorkspace(request);
        ws.create(schema);

        //TODO: set Location header
        return new Response(HTTP_CREATED, MIME_PLAINTEXT, "");
    }

    Response handlePostAddFeatures(Request request, InputStream body) throws IOException {
        VectorData layer = findVectorLayer(request);

        Object obj = new GeoJSONReader().read(body);

        Cursor<Feature> c = layer.cursor(new Query().append());

        if (obj instanceof Feature) {
            Features.copy((Feature)obj, c.next());
            c.write();
        }
        else if (obj instanceof List) {
            for (Feature f : (List<Feature>)obj) {
                Features.copy(f, c.next());
                c.write();
            }
        }

        //TODO: set Location header
        return new Response(HTTP_CREATED, MIME_PLAINTEXT, "");
    }

    VectorData findVectorLayer(Request request) throws IOException {
        Workspace ws = findWorkspace(request);

        Matcher m = (Matcher) request.getContext().get(Matcher.class);
        Dataset l = ws.get(m.group(2));
        if (l == null || !(l instanceof VectorData)) {
            //no such layer
            throw new HttpException(HTTP_NOTFOUND, "No such feature layer: " + m.group(0));
        }

        return (VectorData) l;
    }

    Workspace findWorkspace(Request request) throws IOException {
        Matcher m = (Matcher) request.getContext().get(Matcher.class);

        String key = m.group(1);
        Workspace ws = reg.get(key);
        if ( ws == null) {
            throw new HttpException(HTTP_NOTFOUND, "No such workspace: " +key);
        }

        return ws;
    }

    Envelope parseBBOX(String bbox) {
        String[] split = bbox.split(",");
        return new Envelope(Double.parseDouble(split[0]), Double.parseDouble(split[2]), 
            Double.parseDouble(split[1]), Double.parseDouble(split[3]));
    }

    JSONObject parseJSON(InputStream body) throws JSONException, IOException {
        BufferedReader r  = new BufferedReader(new InputStreamReader(body));

        StringBuilder buf = new StringBuilder();
        String line = null;
        while((line = r.readLine()) != null) {
            buf.append(line);
        }

        
        return new JSONObject(buf.toString());
    }
}
