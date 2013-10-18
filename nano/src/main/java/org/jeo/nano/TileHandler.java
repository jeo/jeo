package org.jeo.nano;

import static org.jeo.nano.NanoHTTPD.HTTP_BADREQUEST;
import static org.jeo.nano.NanoHTTPD.HTTP_INTERNALERROR;
import static org.jeo.nano.NanoHTTPD.HTTP_NOTFOUND;
import static org.jeo.nano.NanoHTTPD.HTTP_OK;
import static org.jeo.nano.NanoHTTPD.MIME_HTML;
import static org.jeo.nano.NanoHTTPD.MIME_PLAINTEXT;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jeo.data.Dataset;
import org.jeo.data.Registry;
import org.jeo.data.Tile;
import org.jeo.data.TileGrid;
import org.jeo.data.TileDataset;
import org.jeo.data.TilePyramid;
import org.jeo.data.TilePyramid.Origin;
import org.jeo.geom.Envelopes;
import org.jeo.nano.NanoHTTPD.Response;

public class TileHandler extends Handler {

    /* /tiles/<workspace>/<layer>/<z>/<x>/<y>.<format.  */
    static final Pattern TILES_URI_RE = Pattern.compile( 
        //"/tiles/([^/]+)/([^/]+)/(\\d+)/+(\\d+)/+(\\d+).(\\w+)", Pattern.CASE_INSENSITIVE);
        "/tiles/((?:[^/]+/)?[^/]+)/(\\d+)/+(\\d+)/+(\\d+).(\\w+)", Pattern.CASE_INSENSITIVE);
    
    @Override
    public boolean canHandle(Request request, NanoJeoServer server) {
        Matcher m = TILES_URI_RE.matcher(request.getUri());
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
            TileDataset layer = findTileLayer(request, server);

            //get the tile index
            Tile t = parseTileIndex(request);

            //check for tile origin and map if necessary
            Properties q = request.getParms();
            if (q != null && q.containsKey("origin")) {
                String o = q.getProperty("origin").toUpperCase();
                Origin origin = Origin.valueOf(o);
                if (origin == null) {
                    return new Response(HTTP_INTERNALERROR, MIME_PLAINTEXT, 
                        "Illegal origin parameter: " + o + ", must be one of: " + Origin.values());
                }

                Tile u = layer.pyramid().realign(t, origin);
                if (u == null) {
                    return new Response(HTTP_INTERNALERROR, MIME_PLAINTEXT, 
                        "No tile grid for zoom level " + t.getZ());
                }
                t = u;
            }

            String format = parseFormat(request);
    
            if ("html".equalsIgnoreCase(format)) {
                return getAsHTML(t, layer, request, server);
            }
            else {
                return getAsImage(t, layer, request, server);
            }
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public Response getAsHTML(Tile tile, TileDataset layer, Request request, NanoJeoServer server) 
        throws IOException {

        Map<String,String> vars = new HashMap<String, String>();
        vars.put("name", layer.getName());
        vars.put("path", parseLayerPath(request));
        vars.put("max_bbox", Envelopes.toString(layer.bounds()));
        
        vars.put("bbox", Envelopes.toString(layer.pyramid().bounds(tile)));

        return new Response(HTTP_OK, MIME_HTML, renderTemplate("tile.html", vars));
    }

    public Response getAsImage(Tile tile, TileDataset layer, Request request, NanoJeoServer server) {
        try {
           
            Tile t = layer.read(tile.getZ(), tile.getX(), tile.getY());
            if (t == null) {
                return new Response(HTTP_NOTFOUND, MIME_PLAINTEXT, String.format(
                    "No such tile z = %d, x = %d, y = %d", tile.getZ(), tile.getX(), tile.getY()));
            }
    
            return new Response(HTTP_OK, t.getMimeType(), new ByteArrayInputStream(t.getData()));
        }
        catch(IOException e) {
            return new Response(HTTP_INTERNALERROR, MIME_PLAINTEXT, e.getLocalizedMessage());
        }
    }

    TileDataset findTileLayer(Request request, NanoJeoServer server) throws IOException {
        String path = parseLayerPath(request);
        Dataset l = findDataset(path, server.getRegistry());
        
        if (!(l instanceof TileDataset)) {
            // not a tile set
            throw new HttpException(HTTP_NOTFOUND, "No such tile layer: " + path);
        }

        return (TileDataset) l;
    }

    String parseLayerPath(Request request) {
        Matcher m = (Matcher) request.getContext().get(Matcher.class);
        return m.group(1);
    }

    String parseFormat(Request request) throws IOException {
        Matcher m = (Matcher) request.getContext().get(Matcher.class);

        if (m.groupCount() > 1 && m.group(2) != null) {
            return m.group(5);
        }

        return null;
    }

    Tile parseTileIndex(Request request) throws IOException {
        Matcher m = (Matcher) request.getContext().get(Matcher.class);

        int z = Integer.parseInt(m.group(2));
        int x = Integer.parseInt(m.group(3));
        int y = Integer.parseInt(m.group(4));

        return new Tile(z, x, y);
    }
}
