package org.jeo.nano;

import static org.jeo.nano.NanoHTTPD.HTTP_METHOD_NOT_ALLOWED;
import static org.jeo.nano.NanoHTTPD.HTTP_NOTFOUND;
import static org.jeo.nano.NanoHTTPD.HTTP_OK;
import static org.jeo.nano.NanoHTTPD.MIME_HTML;
import static org.jeo.nano.NanoHTTPD.MIME_PLAINTEXT;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jeo.data.Dataset;
import org.jeo.data.Query;
import org.jeo.data.VectorDataset;
import org.jeo.feature.Schema;
import org.jeo.geom.Envelopes;
import org.jeo.geom.Geom;
import org.jeo.map.CartoCSS;
import org.jeo.map.Map;
import org.jeo.map.MapBuilder;
import org.jeo.map.Style;
import org.jeo.nano.NanoHTTPD.Response;
import org.jeo.proj.Proj;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;

public class MapHandler extends Handler {

    static final Logger LOG = LoggerFactory.getLogger(NanoJeoServer.class);

    static final String MIME_PNG = "image/png";
            
    // /features/<workspace>[/<layer>]
    static final Pattern MAP_URI_RE = 
        //Pattern.compile("/features/([^/]+)(?:/([^/]+))?/?", Pattern.CASE_INSENSITIVE);
        Pattern.compile("/maps/((?:\\w+/)?\\w+)(?:\\.(\\w+))?/?", Pattern.CASE_INSENSITIVE);

    MapRenderer renderer;

    public MapHandler(MapRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public boolean canHandle(Request request, NanoJeoServer server) {
        Matcher m = MAP_URI_RE.matcher(request.getUri());
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
    
            return new Response(HTTP_METHOD_NOT_ALLOWED, MIME_PLAINTEXT, "");
        }
        catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    Response handleGet(Request request, NanoJeoServer server) throws IOException {
        VectorDataset layer = findVectorLayer(request, server);
        String format = parseFormat(request);

        if ("html".equalsIgnoreCase(format)) {
            return getAsHTML(layer, request, server);
        }
        else {
            return getAsImage(layer, request, server);
        }
    }

    Response getAsHTML(VectorDataset layer, Request request, NanoJeoServer server) 
            throws IOException {

        Properties p = request.getParms();

        HashMap<String,String> vars = new HashMap<String, String>();
        vars.put("name", layer.getName());
        vars.put("path", parseLayerPath(request));
        vars.put("style", p.containsKey("styles") ? p.getProperty("styles") : "");

        Envelope bbox = p.containsKey("bbox") ? parseBBOX(p.getProperty("bbox")) : null;
        CoordinateReferenceSystem crs = 
            p.containsKey("srs") ? Proj.crs(p.getProperty("srs")) : null;

        if (bbox == null) {
            //use layer bounds
            bbox = layer.bounds();

            // may have to reproject it
            if (crs != null) {
                CoordinateReferenceSystem lcrs = layer.crs();
                if (lcrs != null && !Proj.equal(crs, lcrs)) {
                    bbox = Proj.reproject(bbox, lcrs, crs);
                }
            }
        }
        vars.put("bbox", Envelopes.toString(bbox));

        if (p.containsKey("srs")) {
            vars.put("srs", p.getProperty("srs"));
        }
        else {
            crs = layer.crs();
            String srs = null;
            if (crs != null) {
                Integer epsg = Proj.epsgCode(crs);
                if (epsg != null) {
                    srs = "epsg:" + epsg; 
                }
            }
            
            vars.put("srs", srs != null ? srs : "epsg:4326");
        }

        return new Response(HTTP_OK, MIME_HTML, renderTemplate("map.html", vars));
    }

    Response getAsImage(VectorDataset layer, Request request, NanoJeoServer server) 
            throws IOException {

        // create the map
        MapBuilder mb = Map.build().layer(layer);

        Schema schema = layer.schema();

        Properties p = request.getParms();

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
        if (p.containsKey("styles") && !p.getProperty("styles").isEmpty()) {
            String s = p.getProperty("styles");
            style = (Style) server.getRegistry().get(s);
            if (style == null) {
                throw new HttpException(HTTP_NOTFOUND, "No such style: " + s);
            }
        }
        else {
            // create some style
            if (schema.geometry() != null) {
                switch(Geom.Type.from(schema.geometry().getType())) {
                case POINT:
                case MULTIPOINT:
                    style = Style.build().select("*").set(CartoCSS.MARKER_FILE, "black").style();
                    break;
                case POLYGON:
                case MULTIPOLYGON:
                    style = Style.build().select("*")
                        .set(CartoCSS.POLYGON_FILL, "gray").set(CartoCSS.POLYGON_OPACITY, 0.75).style();
                    break;
                default:
                    style = Style.build().select("*").set(CartoCSS.LINE_COLOR, "black").style();
                }
            }
        }
        mb.style(style);

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        renderer.render(mb.map(), bout);

        return new Response(HTTP_OK, MIME_PNG, new ByteArrayInputStream(bout.toByteArray()));
    }

    VectorDataset findVectorLayer(Request request, NanoJeoServer server) throws IOException {
        Matcher m = (Matcher) request.getContext().get(Matcher.class);

        Dataset l = findDataset(m.group(1), server.getRegistry());
        if (l == null || !(l instanceof VectorDataset)) {
            //no such layer
            throw new HttpException(HTTP_NOTFOUND, "No such feature layer: " + m.group(0));
        }

        return (VectorDataset) l;
    }

    Envelope parseBBOX(String bbox) {
        String[] split = bbox.split(",");
        return new Envelope(Double.parseDouble(split[0]), Double.parseDouble(split[2]), 
            Double.parseDouble(split[1]), Double.parseDouble(split[3]));
    }

    String parseFormat(Request request) throws IOException {
        Matcher m = (Matcher) request.getContext().get(Matcher.class);

        if (m.groupCount() > 1 && m.group(2) != null) {
            return m.group(2);
        }

        return null;
    }

    String parseLayerPath(Request request) {
        Matcher m = (Matcher) request.getContext().get(Matcher.class);
        return m.group(1);
    }

}
