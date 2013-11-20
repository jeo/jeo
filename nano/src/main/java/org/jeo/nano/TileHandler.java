package org.jeo.nano;

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

import org.jeo.data.Cursor;
import org.jeo.data.Dataset;
import org.jeo.data.Tile;
import org.jeo.data.TileDataset;
import org.jeo.data.TileGrid;
import org.jeo.data.TilePyramid;
import org.jeo.data.TilePyramid.Origin;
import org.jeo.data.Workspace;
import org.jeo.geom.Envelopes;
import org.jeo.nano.NanoHTTPD.Response;
import org.jeo.util.Pair;

public class TileHandler extends Handler {

    /* /tiles/<workspace>/<layer>/<z>/<x>/<y>.<format.  */
    static final Pattern TILES_URI_RE = Pattern.compile( 
        "/tiles(?:/([\\w-]+)(?:/([\\w-]+)))(?:/(\\d+)/+(\\d+)/+(\\d+))?(?:\\.(\\w+))?", Pattern.CASE_INSENSITIVE);
    
    @Override
    public boolean canHandle(Request request, NanoServer server) {
        return match(request, TILES_URI_RE);
    }
    
    @Override
    public Response handle(Request request, NanoServer server) {
        try {
            Pair<TileDataset,Workspace> p = findTileLayer(request, server);
            TileDataset layer = p.first();

            try {
                //get the tile index
                Tile t = parseTileIndex(request);
                if (t == null) {
                    // no tile index specified pick the first one
                    t = getFirstTile(layer);
                }

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
            finally {
                layer.close();
                Workspace ws = p.second();
                if (ws != null) {
                    ws.close();
                }
            }
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    Tile getFirstTile(TileDataset layer) throws IOException {
        TilePyramid pyr = layer.pyramid();
        TileGrid grid = pyr.getGrids().get(0);

        Cursor<Tile> c = layer.read(grid.getZ(), grid.getZ(), -1, -1, -1, -1);
        try {
            if (c.hasNext()) {
                return c.next();
            }
        }
        finally {
            c.close();
        }

        throw new HttpException(HTTP_NOTFOUND, "unable to locate first tile");
    }

    public Response getAsHTML(Tile tile, TileDataset layer, Request request, NanoServer server) 
        throws IOException {

        Map<String,String> vars = new HashMap<String, String>();
        vars.put("name", layer.getName());
        vars.put("path", createPath(request));
        vars.put("max_bbox", Envelopes.toString(layer.bounds()));
        
        vars.put("bbox", Envelopes.toString(layer.pyramid().bounds(tile)));

        return new Response(HTTP_OK, MIME_HTML, renderTemplate("tile.html", vars));
    }

    public Response getAsImage(Tile tile, TileDataset layer, Request request, NanoServer server) {
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

    Pair<TileDataset,Workspace> findTileLayer(Request request, NanoServer server) throws IOException {
        Pair<Dataset,Workspace> p = findDataset(request, server.getRegistry());
        
        Dataset l = p.first();
        if (!(l instanceof TileDataset)) {
            // not a tile set
            throw new HttpException(HTTP_NOTFOUND, "No such tile layer at: " + request.getUri());
        }

        return Pair.of((TileDataset)l, p.second());
    }

    String parseFormat(Request request) throws IOException {
        Matcher m = (Matcher) request.getContext().get(Matcher.class);
        return m.group(6);
    }

    Tile parseTileIndex(Request request) throws IOException {
        Matcher m = (Matcher) request.getContext().get(Matcher.class);

        if (m.group(3) != null) {
            int z = Integer.parseInt(m.group(3));
            int x = Integer.parseInt(m.group(4));
            int y = Integer.parseInt(m.group(5));
            return new Tile(z, x, y);   
        }
        
        return null;
    }
}
