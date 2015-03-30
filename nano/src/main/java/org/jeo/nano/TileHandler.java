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

import static org.jeo.nano.NanoHTTPD.HTTP_INTERNALERROR;
import static org.jeo.nano.NanoHTTPD.HTTP_NOTFOUND;
import static org.jeo.nano.NanoHTTPD.HTTP_OK;
import static org.jeo.nano.NanoHTTPD.MIME_HTML;
import static org.jeo.nano.NanoHTTPD.MIME_PLAINTEXT;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jeo.data.Cursor;
import org.jeo.data.Dataset;
import org.jeo.tile.Tile;
import org.jeo.tile.TileDataset;
import org.jeo.tile.TileGrid;
import org.jeo.tile.TilePyramid;
import org.jeo.tile.TilePyramid.Origin;
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
            Pair<Workspace, TileDataset> p = findTileLayer(request, server);
            TileDataset layer = p.second;

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
                    String o = q.getProperty("origin").toUpperCase(Locale.ROOT);
                    Origin origin = Origin.valueOf(o);
                    if (origin == null) {
                        return new Response(HTTP_INTERNALERROR, MIME_PLAINTEXT, 
                            "Illegal origin parameter: " + o + ", must be one of: " + Origin.values());
                    }
    
                    Tile u = layer.pyramid().realign(t, origin);
                    if (u == null) {
                        return new Response(HTTP_INTERNALERROR, MIME_PLAINTEXT, 
                            "No tile grid for zoom level " + t.z());
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
                Workspace ws = p.first;
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
        TileGrid grid = pyr.grids().get(0);

        Cursor<Tile> c = layer.read(grid.z(), grid.z(), -1, -1, -1, -1);
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
        vars.put("name", layer.name());
        vars.put("path", createPath(request));
        vars.put("max_bbox", Envelopes.toString(layer.bounds()));
        
        vars.put("bbox", Envelopes.toString(layer.pyramid().bounds(tile)));

        return new Response(HTTP_OK, MIME_HTML, renderTemplate("tile.html", vars));
    }

    public Response getAsImage(Tile tile, TileDataset layer, Request request, NanoServer server) {
        try {
           
            Tile t = layer.read(tile.z(), tile.x(), tile.y());
            if (t == null) {
                return new Response(HTTP_NOTFOUND, MIME_PLAINTEXT, String.format(
                    Locale.ROOT, "No such tile z = %d, x = %d, y = %d", tile.z(), tile.x(), tile.y()));
            }
    
            return new Response(HTTP_OK, t.mimeType(), new ByteArrayInputStream(t.data()));
        }
        catch(IOException e) {
            return new Response(HTTP_INTERNALERROR, MIME_PLAINTEXT, e.getLocalizedMessage());
        }
    }

    Pair<Workspace, TileDataset> findTileLayer(Request request, NanoServer server) throws IOException {
        Pair<Workspace, ? extends Dataset> p = findWorkspaceOrDataset(request, server.getRegistry());
        
        if (!(p.second instanceof TileDataset)) {
            // not a tile set
            throw new HttpException(HTTP_NOTFOUND, "No such tile layer at: " + request.getUri());
        }

        return (Pair<Workspace, TileDataset>) p;
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
