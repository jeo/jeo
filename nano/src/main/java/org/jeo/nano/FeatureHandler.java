/* Copyright 2013 The jeo project. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jeo.nano;

import static org.jeo.nano.NanoHTTPD.HTTP_BADREQUEST;
import static org.jeo.nano.NanoHTTPD.HTTP_CREATED;
import static org.jeo.nano.NanoHTTPD.HTTP_FORBIDDEN;
import static org.jeo.nano.NanoHTTPD.HTTP_METHOD_NOT_ALLOWED;
import static org.jeo.nano.NanoHTTPD.HTTP_NOTFOUND;
import static org.jeo.nano.NanoHTTPD.HTTP_OK;
import static org.jeo.nano.NanoHTTPD.MIME_HTML;
import static org.jeo.nano.NanoHTTPD.MIME_JSON;
import static org.jeo.nano.NanoHTTPD.MIME_PLAINTEXT;
import static org.jeo.nano.NanoHTTPD.MIME_PNG;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jeo.data.Cursor;
import org.jeo.data.Dataset;
import org.jeo.data.Query;
import org.jeo.data.VectorDataset;
import org.jeo.data.Workspace;
import org.jeo.feature.Feature;
import org.jeo.feature.Features;
import org.jeo.feature.Field;
import org.jeo.feature.Schema;
import org.jeo.filter.Filter;
import org.jeo.filter.cql.CQL;
import org.jeo.filter.cql.ParseException;
import org.jeo.geojson.GeoJSONReader;
import org.jeo.geojson.GeoJSONWriter;
import org.jeo.geom.Envelopes;
import org.jeo.geom.Geom;
import org.jeo.json.JSONObject;
import org.jeo.json.JSONValue;
import org.jeo.map.CartoCSS;
import org.jeo.map.MapBuilder;
import org.jeo.map.Style;
import org.jeo.nano.NanoHTTPD.Response;
import org.jeo.proj.Proj;
import org.jeo.util.Pair;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;

import org.jeo.filter.Id;
import org.jeo.filter.Literal;

public class FeatureHandler extends Handler {

    static final Logger LOG = LoggerFactory.getLogger(NanoServer.class);

    // /features/<workspace>[/<layer>][/<id>]
    static final Pattern FEATURES_URI_RE =
        Pattern.compile("/features(?:/([\\w-]+)(?:/([\\w-]+))?)(?:/([\\w-]+))?(?:\\.([\\w]+))?/?", Pattern.CASE_INSENSITIVE);

    MapRenderer renderer;

    public FeatureHandler() {
        this(null);
    }

    public FeatureHandler(MapRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public void init(NanoServer server) {
        if (renderer == null) {
            renderer = server.getRenderer();
        }
    }

    @Override
    public boolean canHandle(Request request, NanoServer server) {
        return match(request, FEATURES_URI_RE);
    }

    @Override
    public Response handle(Request request, NanoServer server) {
        try {
            if ("GET".equalsIgnoreCase(request.getMethod())) {
                return handleGet(request, server);
            }
            else if ("POST".equalsIgnoreCase(request.getMethod())) {
                return handlePost(request, server);
            }
            else if ("PUT".equalsIgnoreCase(request.getMethod())) {
                return handlePut(request, server);
            }
            else if ("DELETE".equalsIgnoreCase(request.getMethod())) {
                return handleDelete(request, server);
            }
    
            return new Response(HTTP_METHOD_NOT_ALLOWED, MIME_PLAINTEXT, "");
        }
        catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    Response handleGet(Request request, NanoServer server) throws IOException {
        Pair<Workspace,VectorDataset> p = findVectorLayer(request, server);
        String format = parseFormat(request);

        VectorDataset layer = p.second();
        Response resp;
        if ("html".equalsIgnoreCase(format)) {
            resp = getAsHTML(layer, request, server);
        }
        else if ("png".equalsIgnoreCase(format)) {
            resp = getAsPNG(layer, request, server);
        }
        else {
            resp = getAsJSON(layer, request, server);
        }
        resp.toClose(layer, p.first());
        return resp;
    }

    Response getAsJSON(VectorDataset layer, Request request, NanoServer server) 
        throws IOException {

        Query q = buildQuery(layer, request);
        
        final Cursor<Feature> c = layer.cursor(q);

        // if requesting a specific feature, fail if not found
        String fid = parseFeatureId(request);
        if (fid != null) {
            if (! c.hasNext()) {
                throw new HttpException(HTTP_NOTFOUND, "Unable to locate feature at " + request.uri);
            }
        }

        return new Response(HTTP_OK, MIME_JSON, new Response.Content() {

            GeoJSONWriter w;

            @Override
            public void write(OutputStream output) throws IOException {
                w = new GeoJSONWriter(new OutputStreamWriter(output));
                w.featureCollection(c);
                w.flush();
            }

            @Override
            public void close() throws IOException {
                c.close();
            }
        });
    }

    Query buildQuery(VectorDataset layer, Request request) throws IOException {
        Properties p = request.getParms();

        //parse the bbox
        Envelope bbox = null;

        if (p.containsKey("bbox")) {
            bbox = parseBBOX(p.getProperty("bbox"));
        }

        Query q = new Query();

        String fid = parseFeatureId(request);

        if (fid != null) {
            // @todo if fid is provided, should other parameters be 'errors'?
            q.filter(new Id(new Literal(fid)));
        }

        if (p.containsKey("srs")) {
            CoordinateReferenceSystem to = parseCRS(p);
            CoordinateReferenceSystem from = layer.crs();

            // may have to back eproject bbox
            if (bbox != null && from != null && !Proj.equal(from, to)) {
                bbox = Proj.reproject(bbox, to, from);
            }

            q.reproject(from, to);
        }

        if (bbox != null) {
            q.bounds(bbox);
        }

        if (p.containsKey("limit")) {
            q.limit(Integer.parseInt(p.getProperty("limit")));
        }

        if (p.containsKey("offset")) {
            q.offset(Integer.parseInt(p.getProperty("offset")));
        }

        if (p.containsKey("filter")) {
            q.filter(parseFilter(p.getProperty("filter")));
        }

        return q;
    }

    Response getAsHTML(VectorDataset layer, Request request, NanoServer server) 
        throws IOException {

        Map<String,String> vars = new HashMap<String, String>();
        vars.put("name", layer.getName());
        vars.put("path", createPath(request));

        Properties p = request.getParms();

        Envelope bbox = p.containsKey("bbox") ? parseBBOX(p.getProperty("bbox")) : null;
        CoordinateReferenceSystem crs = parseCRS(p);
        if (crs == null) {
            // no crs specified, use layer crs
            crs = layer.crs();
        }

        if (bbox == null) {
            //use layer bounds
            bbox = layer.bounds();

            // may have to reproject it
            if (crs != null) {
                CoordinateReferenceSystem lcrs = layer.crs();
                bbox = reprojectIfNecessary(bbox, lcrs, crs);
            }
        }

        vars.put("bbox", Envelopes.toString(bbox));

        if (p.containsKey("srs")) {
            vars.put("srs", p.getProperty("srs"));
        }
        else {
            String srs = null;
            if (crs != null) {
                Integer epsg = Proj.epsgCode(crs);
                if (epsg != null) {
                    srs = "epsg:" + epsg; 
                }
            }

            if (srs != null) {
                vars.put("srs", srs);
            }
            else {
                // fall back to epsg:4326, may have to reproject bbox again
                vars.put("srs", "epsg:4326");
                vars.put("bbox", Envelopes.toString(reprojectIfNecessary(bbox, crs, Proj.EPSG_4326)));
            }
        }

        return new Response(HTTP_OK, MIME_HTML, renderTemplate("feature.html", vars));
    }

    Envelope reprojectIfNecessary(Envelope bbox, 
        CoordinateReferenceSystem from, CoordinateReferenceSystem to) {
        if (from != null && !Proj.equal(to, from)) {
            bbox = Proj.reproject(bbox, from, to);
        }
        return bbox;
    }

    Response getAsPNG(VectorDataset layer, Request request, NanoServer server) throws IOException {
        if (renderer == null) {
            throw new HttpException(HTTP_FORBIDDEN, "No rendering engine avaialble, map endpoint unavailable"); 
        }

        Properties p = request.getParms();
        Filter filter = null;
        
        // create the map, and add the layer
        if (p.containsKey("filter")) {
            filter = parseFilter(p.getProperty("filter"));
        }

        MapBuilder mb = new MapBuilder().layer(layer, filter);

        Schema schema = layer.schema();

        if (p.containsKey("srs")) {
            mb.crs(Proj.crs(p.getProperty("srs")));
        }

        if (p.containsKey("bbox")) {
            mb.bounds(parseBBOX(p.getProperty("bbox")));
        }

        //parse the image dimensions
        Integer width = p.containsKey("width") ? Integer.parseInt(p.getProperty("width")) : 256;
        Integer height = p.containsKey("height") ? Integer.parseInt(p.getProperty("height")) : width;
        mb.size(width, height);

        Style style = null;
        if (p.containsKey("style") && !p.getProperty("style").isEmpty()) {
            String s = p.getProperty("style");
            try {
                style = server.getRegistry().get(s, Style.class);
                if (style == null) {
                    throw new HttpException(HTTP_NOTFOUND, "No such style: " + s);
                }
            }
            catch(ClassCastException e) {
                throw new HttpException(HTTP_BADREQUEST, String.format(
                    "Object %s is not a style", s));
            }
        }
        else {
            // create some style
            if (schema.geometry() != null) {
                switch(Geom.Type.from(schema.geometry().getType())) {
                case POINT:
                case MULTIPOINT:
                    style = Style.build().select("*").set(CartoCSS.MARKER_FILL, "gray").style();
                    break;
                case POLYGON:
                case MULTIPOLYGON:
                    style = Style.build().select("*")
                        .set(CartoCSS.POLYGON_FILL, "gray").set(CartoCSS.POLYGON_OPACITY, 0.75).style();
                    break;
                default:
                    style = Style.build().select("*").set(CartoCSS.LINE_COLOR, "gray").style();
                }
            }
        }
        mb.style(style);

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        renderer.render(mb.map(), bout);

        return new Response(HTTP_OK, MIME_PNG, new ByteArrayInputStream(bout.toByteArray()));
    }

    Response handlePost(Request request, NanoServer server) throws IOException {
        Matcher m = (Matcher) request.getContext().get(Matcher.class);
        String dataSet = m.group(2);

        if (dataSet == null) {
            return handleCreateLayer(request, server);
        } else {
            return handlePostAddFeatures(request, server);
        }
    }

    Response handlePut(Request request, NanoServer server) throws IOException {
        String fid = parseFeatureId(request);
        if (fid == null) {
            return handleCreateLayer(request, server);
        } else {
            return handlePutEditFeature(fid, request, server);
        }
    }

    Response handlePutEditFeature(String fid, Request request, NanoServer server) throws IOException {
        Query query = new Query().update().filter(new Id(new Literal(fid)));
        handleWrite(request, server, query, true);

        return new Response(HTTP_OK, MIME_PLAINTEXT, "");
    }

    Response handleCreateLayer(Request request, NanoServer server) throws IOException {
        Matcher m = (Matcher) request.getContext().get(Matcher.class);
        String dataSetName = m.group(2);

        Schema schema = parseSchema(dataSetName, getInput(request));
        Workspace ws = findWorkspace(m.group(1), server.getRegistry());

        try {
            if (ws.get(schema.getName()) != null) {
                String msg = String.format("dataset '%s' already exists in workspace %s",
                        schema.getName(), m.group(1));
                throw new HttpException(HTTP_BADREQUEST, msg);
            }
            ws.create(schema);
        } finally {
            ws.close();
        }

        //TODO: set Location header
        return new Response(HTTP_CREATED, MIME_PLAINTEXT, "");
    }

    Response handlePostAddFeatures(Request request, NanoServer server) throws IOException {
        Query query = new Query().append();
        handleWrite(request, server, query, false);
        //TODO: set Location header
        return new Response(HTTP_CREATED, MIME_PLAINTEXT, "");
    }

    void handleWrite(Request request, NanoServer server, Query query, boolean shouldExist) throws IOException {
        Pair<Workspace, VectorDataset> p = findVectorLayer(request, server);

        Object obj = new GeoJSONReader().read(getInput(request));

        VectorDataset layer = p.second();
        try {
            Cursor<Feature> c = layer.cursor(query);
            if (shouldExist && ! c.hasNext()) {
                throw new HttpException(HTTP_NOTFOUND, "requested feature does not exist : " + request.getUri());
            }
            try {
                if (obj instanceof Feature) {
                    Features.copy((Feature)obj, c.next());
                    c.write();
                }
                else if (obj instanceof Iterable) {
                    for (Feature f : (Iterable<Feature>)obj) {
                        Features.copy(f, c.next());
                        c.write();
                    }
                }
                else {
                    throw new HttpException(HTTP_BADREQUEST, "unable to add features from: " + obj);
                }
            }
            finally {
                c.close();
            }
        }
        finally {
            layer.close();
            Workspace ws = p.first();
            if (ws != null ) {
                ws.close();
            }
        }
    }

    Response handleDelete(Request request, NanoServer server) throws IOException {
        String fid = parseFeatureId(request);
        if (fid == null) {
            throw new HttpException(HTTP_BADREQUEST, "must provide feature id for PUT");
        }

        Query query = new Query().update().filter(new Id(new Literal(fid)));
        Pair<Workspace, VectorDataset> p = findVectorLayer(request, server);

        VectorDataset layer = p.second();
        Cursor<Feature> c = layer.cursor(query);
        try {
            if (! c.hasNext()) {
                throw new HttpException(HTTP_NOTFOUND, "requested feature does not exist : " + request.getUri());
            }
            c.next();
            c.remove();
        } finally {
            c.close();
            layer.close();
            p.first().close();
        }
        return new Response(HTTP_OK, MIME_PLAINTEXT, "");
    }

    Pair<Workspace, VectorDataset> findVectorLayer(Request request, NanoServer server) throws IOException {
        Pair<Workspace, ? extends Dataset> p = findWorkspaceOrDataset(request, server.getRegistry());
        if (!(p.second() instanceof VectorDataset)) {
            throw new HttpException(HTTP_BADREQUEST, request.getUri() + " is not a feature layer");
        }
        return (Pair<Workspace, VectorDataset>) p;
    }

    InputStream getInput(Request request) throws IOException {
        String file = request.getFiles().getProperty("content");
        return new BufferedInputStream(new FileInputStream(file));
    }

    String parseFormat(Request request) throws IOException {
        Matcher m = (Matcher) request.getContext().get(Matcher.class);

        return m.group(4) != null ? m.group(4) : null;
    }

    String parseFeatureId(Request request) throws IOException {
        Matcher m = (Matcher) request.getContext().get(Matcher.class);

        return m.group(3) != null ? m.group(3) : null;
    }

    Envelope parseBBOX(String bbox) {
        String[] split = bbox.split(",");
        return new Envelope(Double.parseDouble(split[0]), Double.parseDouble(split[2]), 
            Double.parseDouble(split[1]), Double.parseDouble(split[3]));
    }

    Filter parseFilter(String cql) {
        try {
            return CQL.parse(cql);
        } catch (ParseException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Error parsing cql filter", e);
            }
            String message = "Unsupported cql filter: " + cql + "\n";
            message += e.getMessage();
            throw new HttpException(HTTP_BADREQUEST, message);
        }
    }

    JSONObject parseJSON(InputStream body) throws IOException {
        return (JSONObject) JSONValue.parse(new InputStreamReader(body));
    }

    Schema parseSchema(String name, InputStream body) {

        JSONObject obj = (JSONObject) JSONValue.parse(new InputStreamReader(body));

        //not part of GeoJSON
        if (name == null) {
            name = (String) obj.get("name");

            if (name == null) {
                throw new IllegalArgumentException("Object must specify name property");
            }
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
                 crs = parseCRS(prop.get("crs").toString());
             }
 
             fields.add(new Field(key, clazz, crs));
         }
 
         return new Schema(name, fields);
    }
}
