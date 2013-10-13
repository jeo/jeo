package org.jeo.nano;

import static org.jeo.nano.NanoHTTPD.HTTP_BADREQUEST;
import static org.jeo.nano.NanoHTTPD.HTTP_INTERNALERROR;
import static org.jeo.nano.NanoHTTPD.HTTP_NOTFOUND;
import static org.jeo.nano.NanoHTTPD.HTTP_OK;
import static org.jeo.nano.NanoHTTPD.MIME_PLAINTEXT;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jeo.data.Dataset;
import org.jeo.data.Registry;
import org.jeo.data.Tile;
import org.jeo.data.TileGrid;
import org.jeo.data.TileDataset;
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
        Matcher m = (Matcher) request.getContext().get(Matcher.class);
        if (m == null) {
            throw new IllegalStateException("No matcher");
        }

        try {
            Registry reg = server.getRegistry();
            Dataset l = findDataset(m.group(1), reg);
    
            if (!(l instanceof TileDataset)) {
                // not a tile set
                return new Response(HTTP_BADREQUEST, MIME_PLAINTEXT, 
                    "Layer " + m.group(2) + " not a tile set");
            }
    
            TileDataset ts = (TileDataset) l;
    
            //get teh tile index
            long z = Long.parseLong(m.group(2));
            long x = Long.parseLong(m.group(3));
            long y = Long.parseLong(m.group(4));
    
            //check for flipy flag
            Properties q = request.getParms();
            if (q != null && q.containsKey("flipy") && Boolean.valueOf(q.getProperty("flipy"))) {
                TileGrid g =  ts.pyramid().grid((int)z);
                if (g == null) {
                    return new Response(HTTP_INTERNALERROR, MIME_PLAINTEXT, "No tile grid for zoom level " + z);
                }
                y = g.getHeight() - (y+1);
            }
    
            Tile t = ts.read(z, x, y);
            if (t == null) {
                return new Response(HTTP_NOTFOUND, MIME_PLAINTEXT, 
                    String.format("No such tile z = %d, x = %d, y = %d", z, x, y));
            }
    
            return new Response(HTTP_OK, t.getMimeType(), new ByteArrayInputStream(t.getData()));
        }
        catch(IOException e) {
            return new Response(HTTP_INTERNALERROR, MIME_PLAINTEXT, e.getLocalizedMessage());
        }
    }

}
