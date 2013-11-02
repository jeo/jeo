package org.jeo.nano;

import static org.jeo.nano.NanoHTTPD.HTTP_OK;
import static org.jeo.nano.NanoHTTPD.HTTP_BADREQUEST;
import static org.jeo.nano.NanoHTTPD.HTTP_NOTFOUND;
import static org.jeo.nano.NanoHTTPD.MIME_JSON;

import java.io.IOException;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jeo.data.Dataset;
import org.jeo.data.DatasetHandle;
import org.jeo.data.Disposable;
import org.jeo.data.Driver;
import org.jeo.data.Query;
import org.jeo.data.DataRepository;
import org.jeo.data.TileDataset;
import org.jeo.data.TileGrid;
import org.jeo.data.TilePyramid;
import org.jeo.data.VectorDataset;
import org.jeo.data.Workspace;
import org.jeo.data.WorkspaceHandle;
import org.jeo.feature.Field;
import org.jeo.geojson.GeoJSONWriter;
import org.jeo.geom.Envelopes;
import org.jeo.map.Style;
import org.jeo.nano.NanoHTTPD.Response;
import org.jeo.util.Pair;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

public class DataHandler extends Handler {

    static final Pattern DATA_URI_RE =
        Pattern.compile("/data(?:/(\\w+)(?:/(\\w+))?)?/?", Pattern.CASE_INSENSITIVE);

    @Override
    public boolean canHandle(Request request, NanoServer server) {
        return match(request, DATA_URI_RE);
    }

    @Override
    public Response handle(Request request, NanoServer server) throws Exception {
        DataRepository reg = server.getRegistry();

        Pair<Object,Object> p = findObject(request, reg);

        StringWriter out = new StringWriter();
        GeoJSONWriter writer = new GeoJSONWriter(out);

        if (p == null) {
            // dump root
            handleAll(reg, writer);
        }
        else {
            try {
                Object obj = p.first();
                if (obj instanceof Workspace) {
                    handleWorkspace((Workspace) obj, writer);
                }
                else if (obj instanceof Dataset) {
                    handleDataset((Dataset)obj, writer, request);
                }
                else if (obj instanceof Style) {
                    handleStyle((Style) obj, writer, request);
                }
                else {
                    throw new HttpException(HTTP_BADREQUEST, "unknown object: " + obj);
                }
            }
            finally {
                close(p.first());
                close(p.second());
            }
        }

        writer.flush();
        return new Response(HTTP_OK, MIME_JSON, out.toString());
    }

    void handleAll(DataRepository reg, GeoJSONWriter w) throws IOException {
        w.object();
        
        for (WorkspaceHandle item : reg.list()) {
            w.key(item.getName()).object();

            w.key("type");
            Class<?> t = item.getType();
            if (Workspace.class.isAssignableFrom(t)) {
                w.value("workspace");
            }
            else if (Dataset.class.isAssignableFrom(t)) {
                w.value("dataset");
            }
            else if (Style.class.isAssignableFrom(t)) {
                w.value("style");
            }
            else {
                w.nul();
            }

            Driver<?> drv = item.getDriver();
            w.key("driver").value(drv.getName());

            w.endObject();

        }
        w.endObject();
    }

    void handleWorkspace(Workspace ws, GeoJSONWriter w) throws IOException{
        w.object();

        w.key("type").value("workspace");
        w.key("driver").value(ws.getDriver().getName());
        w.key("datasets").array();

        for (DatasetHandle ref : ws.list()) {
            w.value(ref.getName());
        }

        w.endArray();

        w.endObject();
    }

    void handleDataset(Dataset ds, GeoJSONWriter w, Request req) throws IOException{
        w.object();

        w.key("name").value(ds.getName());
        
        w.key("type");
        if (ds instanceof VectorDataset) {
            w.value("vector");
        }
        else if (ds instanceof TileDataset ){
            w.value("tile");
        }
        else {
            w.nul();
        }

        w.key("driver").value(ds.getDriver().getName());
 
        Envelope bbox = ds.bounds();
        if (!Envelopes.isNull(bbox)) {
            w.key("bbox");
            w.bbox(bbox);
        }
 
         CoordinateReferenceSystem crs = ds.crs();
         if (crs != null) {
            w.key("crs");
            w.array();
            
            for (String s : crs.getParameters()) {
                w.value(s);
            }
        
            w.endArray();
         }

         if (ds instanceof VectorDataset) {
             handleVectorDataset((VectorDataset)ds, w, req);
         }
         else if (ds instanceof TileDataset ){
             handleTileDataset((TileDataset)ds, w, req);
         }

         w.endObject();
    }

    void handleVectorDataset(VectorDataset ds, GeoJSONWriter w, Request req) throws IOException {
        w.key("count").value(ds.count(new Query()));
        w.key("schema").object();

        for (Field fld : ds.schema()) {
            w.key(fld.getName()).value(fld.getType().getSimpleName());
        }

        w.endObject();
        w.key("features").value("/features/" + path(req) + ".json");
    }

    void handleTileDataset(TileDataset ds, GeoJSONWriter w, Request req) throws IOException {
        TilePyramid pyr = ds.pyramid();
        w.key("tilesize").array().value(pyr.getTileWidth())
                .value(pyr.getTileHeight()).endArray();

        w.key("grids").array();
        for (TileGrid grid : ds.pyramid().getGrids()) {
            w.object().key("zoom").value(grid.getZ()).key("width")
                .value(grid.getWidth()).key("height")
                .value(grid.getHeight()).key("res").array()
                .value(grid.getXRes()).value(grid.getYRes()).endArray()
                .endObject();

        }
        w.endArray();

        //TODO: link to first tile?
        w.key("tiles").value("/tiles/" + path(req));
    }

    void handleStyle(Style s, GeoJSONWriter w, Request req) throws IOException {
        w.object()
          .key("type").value("style")
          .key("style").value("/styles/" + path(req))
          .endObject();
    }

    Pair<Object,Object> findObject(Request request, DataRepository reg) throws IOException {
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

    String path(Request req) {
        Matcher m = (Matcher) req.getContext().get(Matcher.class);
        String p = m.group(1);
        if (m.group(2) != null) {
            p += "/" + m.group(2);
        }
        return p;
    }

    void close(Object obj) {
        if (obj instanceof Disposable) {
            ((Disposable)obj).close();
        }
    }

}
