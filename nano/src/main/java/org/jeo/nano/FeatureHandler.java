package org.jeo.nano;

import static org.jeo.nano.NanoHTTPD.HTTP_BADREQUEST;
import static org.jeo.nano.NanoHTTPD.HTTP_CREATED;
import static org.jeo.nano.NanoHTTPD.HTTP_METHOD_NOT_ALLOWED;
import static org.jeo.nano.NanoHTTPD.HTTP_NOTFOUND;
import static org.jeo.nano.NanoHTTPD.HTTP_OK;
import static org.jeo.nano.NanoHTTPD.MIME_JSON;
import static org.jeo.nano.NanoHTTPD.MIME_PLAINTEXT;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jeo.data.Cursor;
import org.jeo.data.Dataset;
import org.jeo.data.Query;
import org.jeo.data.VectorData;
import org.jeo.data.Workspace;
import org.jeo.feature.Feature;
import org.jeo.feature.Features;
import org.jeo.feature.Field;
import org.jeo.feature.Schema;
import org.jeo.geojson.GeoJSONReader;
import org.jeo.geojson.GeoJSONWriter;
import org.jeo.geom.Geom;
import org.jeo.nano.NanoHTTPD.Response;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;

public class FeatureHandler extends Handler {

    static final Logger LOG = LoggerFactory.getLogger(NanoJeoServer.class);

    // /features/<workspace>[/<layer>]
    static final Pattern FEATURES_URI_RE = 
        //Pattern.compile("/features/([^/]+)(?:/([^/]+))?/?", Pattern.CASE_INSENSITIVE);
        Pattern.compile("/features/((?:[^/]+/)?[^/]+)/?", Pattern.CASE_INSENSITIVE);

    @Override
    public boolean canHandle(Request request, NanoJeoServer server) {
        Matcher m = FEATURES_URI_RE.matcher(request.getUri());
        if (m.matches()) {
            //save the matcher
            request.getContext().put(Matcher.class, m);
            return true;
        }
        return false;
    }

    @Override
    public Response handle(Request request, NanoJeoServer server) {
        try {
            if ("GET".equalsIgnoreCase(request.getMethod())) {
                return handleGet(request, server);
            }
            else if ("POST".equalsIgnoreCase(request.getMethod())) {
                return handlePost(request, server);
            }
    
            return new Response(HTTP_METHOD_NOT_ALLOWED, MIME_PLAINTEXT, "");
        }
        catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    Response handleGet(Request request, NanoJeoServer server) throws IOException {
        VectorData layer = findVectorLayer(request, server);

        //parse the bbox
        Properties p = request.getParms();
        if (!p.containsKey("bbox")) {
            return new Response(HTTP_BADREQUEST, MIME_PLAINTEXT, "Request must specify bbox");
        }

        Envelope bbox = parseBBOX(p.getProperty("bbox"));

        Query q = new Query().bounds(bbox);
        if (p.containsKey("limit")) {
            q.limit(Integer.parseInt(p.getProperty("limit")));
        }
        
        Cursor<Feature> c = layer.cursor(q);
        return new Response(HTTP_OK, MIME_JSON, GeoJSONWriter.toString(c));
    }

    Response handlePost(Request request, NanoJeoServer server) throws IOException {
        Matcher m = (Matcher) request.getContext().get(Matcher.class);
        String key = m.group(1);

        String file = request.getFiles().getProperty("content");
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
        try {
            if (!key.contains("/")) {
                return handlePostCreateLayer(request, in, server);
            }
            else {
                return handlePostAddFeatures(request, in, server);
            }
        }
        finally {
            in.close();
        }
    }

    Response handlePostCreateLayer(Request request, InputStream body, NanoJeoServer server) throws IOException {
        Matcher m = (Matcher) request.getContext().get(Matcher.class);
        Workspace ws = findWorkspace(m.group(1), server.getRegistry());
        
        Schema schema = parseSchema(body);
        ws.create(schema);

        //TODO: set Location header
        return new Response(HTTP_CREATED, MIME_PLAINTEXT, "");
    }

    Response handlePostAddFeatures(Request request, InputStream body, NanoJeoServer server) throws IOException {
        VectorData layer = findVectorLayer(request, server);

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

    VectorData findVectorLayer(Request request, NanoJeoServer server) throws IOException {
        Matcher m = (Matcher) request.getContext().get(Matcher.class);

        Dataset l = findDataset(m.group(1), server.getRegistry());
        if (l == null || !(l instanceof VectorData)) {
            //no such layer
            throw new HttpException(HTTP_NOTFOUND, "No such feature layer: " + m.group(0));
        }

        return (VectorData) l;
    }

    Envelope parseBBOX(String bbox) {
        String[] split = bbox.split(",");
        return new Envelope(Double.parseDouble(split[0]), Double.parseDouble(split[2]), 
            Double.parseDouble(split[1]), Double.parseDouble(split[3]));
    }

    JSONObject parseJSON(InputStream body) throws IOException {
        return (JSONObject) JSONValue.parse(new InputStreamReader(body));
    }

    Schema parseSchema(InputStream body) {

        JSONObject obj = (JSONObject) JSONValue.parse(new InputStreamReader(body));

        //not part of GeoJSON
         String name = (String) obj.get("name");
         if (name == null) {
             throw new IllegalArgumentException("Object must specify name property");
         }
 
         JSONObject properties = (JSONObject) obj.get("properties");
         List<Field> fields = new ArrayList<Field>();
 
         for (Iterator<?> it = properties.keySet().iterator(); it.hasNext(); ) {
             String key = it.next().toString();
             JSONObject prop = (JSONObject) properties.get(key);
 
             String type = (String) prop.get("type");
             Class<?> clazz = null; 
 
             //first try as geometry
             if (Geom.Type.from(type) != null) {
                 clazz = Geom.Type.from(type).getType();
             }
             else {
                 //try as a primitive
                 try {
                     clazz = Class.forName("java.lang." + 
                         Character.toUpperCase(type.charAt(0)) + type.substring(1));
                 } catch (ClassNotFoundException e) {
                     throw new IllegalArgumentException("type " +type+ " not supported"); 
                 }
             }
 
             CoordinateReferenceSystem crs = null;
             if (prop.containsKey("crs")) {
                 //crs = readCRS(prop.get("crs"));
             }
 
             fields.add(new Field(key, clazz, crs));
         }
 
         return new Schema(name, fields);
    }
}
