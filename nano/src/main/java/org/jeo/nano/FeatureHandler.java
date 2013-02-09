package org.jeo.nano;

import static org.jeo.nano.NanoHTTPD.HTTP_BADREQUEST;
import static org.jeo.nano.NanoHTTPD.HTTP_CREATED;
import static org.jeo.nano.NanoHTTPD.HTTP_INTERNALERROR;
import static org.jeo.nano.NanoHTTPD.HTTP_METHOD_NOT_ALLOWED;
import static org.jeo.nano.NanoHTTPD.HTTP_NOTFOUND;
import static org.jeo.nano.NanoHTTPD.HTTP_OK;
import static org.jeo.nano.NanoHTTPD.MIME_JSON;
import static org.jeo.nano.NanoHTTPD.MIME_PLAINTEXT;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jeo.data.Cursor;
import org.jeo.data.Layer;
import org.jeo.data.Registry;
import org.jeo.data.Vector;
import org.jeo.feature.Feature;
import org.jeo.geojson.GeoJSON;
import org.jeo.geojson.GeoJSONWriter;
import org.jeo.nano.NanoHTTPD.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;

public class FeatureHandler extends Handler {

    static final Logger LOG = LoggerFactory.getLogger(FeatureHandler.class);

    /** uri pattern, /<layer>/features?bbox=<bbox> */
    static final Pattern FEATURES_URI_RE = 
        Pattern.compile("/(.+)/+features", Pattern.CASE_INSENSITIVE);

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
        Matcher m = (Matcher) request.getContext().get(Matcher.class);

        String key = m.group(1);
        Layer l = reg.get(key);
        if (l == null) {
            //no such layer
            return new Response(HTTP_NOTFOUND, MIME_PLAINTEXT, "No such layer: " + key);
        }

        if (!(l instanceof Vector)) {
            // not a vector layer
            return new Response(HTTP_BADREQUEST, MIME_PLAINTEXT, key + " not a feature layer");
        }

        Vector v = (Vector) l;
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            return handleGet(request, v);
        }
        else if ("POST".equalsIgnoreCase(request.getMethod())) {
            return handlePost(request, v);
        }

        return new Response(HTTP_METHOD_NOT_ALLOWED, MIME_PLAINTEXT, "");
    }

    Response handleGet(Request request, Vector v) {
        //parse the bbox
        Properties q = request.getParms();
        if (!q.containsKey("bbox")) {
            return new Response(HTTP_BADREQUEST, MIME_PLAINTEXT, "Request must specify bbox");
        }

        Envelope bbox = parseBBOX(q.getProperty("bbox"));

        GeoJSONWriter w = new GeoJSONWriter();

        try {
            Cursor<Feature> c = v.read(bbox);
    
            w.featureCollection();
            while(c.hasNext()) {
                w.feature(c.next());
            }
            w.endFeatureCollection();
        }
        catch(IOException e) {
            LOG.warn("Error reading features", e);
            return new Response(HTTP_INTERNALERROR, MIME_PLAINTEXT, 
                "Error reading features: " + e.getLocalizedMessage());
        }

        return new Response(HTTP_OK, MIME_JSON, w.toString());
    }

    Response handlePost(Request request, Vector v) {
        try {
            String file = request.getFiles().getProperty("content");
    
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
            Object obj = GeoJSON.read(in);
            in.close();
    
            if (obj instanceof Feature) {
                v.add((Feature)obj);
            }
            else if (obj instanceof List) {
                for (Feature f : (List<Feature>)obj) {
                    v.add(f);
                }
            }

            return new Response(HTTP_CREATED, MIME_PLAINTEXT, "");
        }
        catch(IOException e) {
            LOG.warn("Error adding features", e);
            return new Response(HTTP_INTERNALERROR, MIME_PLAINTEXT, 
                "Error adding features: " + e.getLocalizedMessage());
        }
    }

    Envelope parseBBOX(String bbox) {
        String[] split = bbox.split(",");
        return new Envelope(Double.parseDouble(split[0]), Double.parseDouble(split[2]), 
            Double.parseDouble(split[1]), Double.parseDouble(split[3]));
    }

}
