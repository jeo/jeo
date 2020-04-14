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
package io.jeo.nano;

import io.jeo.data.Dataset;
import io.jeo.data.Disposable;
import io.jeo.data.Driver;
import io.jeo.data.Handle;
import io.jeo.data.Workspace;
import io.jeo.data.*;
import io.jeo.geojson.GeoJSONWriter;
import io.jeo.json.JeoJSONWriter;
import io.jeo.map.Style;
import io.jeo.nano.NanoHTTPD.Response;
import io.jeo.raster.RasterDataset;
import io.jeo.tile.TileDataset;
import io.jeo.util.Pair;
import io.jeo.vector.VectorDataset;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;

import static io.jeo.nano.NanoHTTPD.HTTP_OK;
import static io.jeo.nano.NanoHTTPD.MIME_JSON;

public class DataHandler extends Handler {

    static final Pattern DATA_URI_RE =
        Pattern.compile("/data(?:/([\\w-]+)(?:/([\\w-]+))?)?/?", Pattern.CASE_INSENSITIVE);

    @Override
    public boolean canHandle(Request request, NanoServer server) {
        return match(request, DATA_URI_RE);
    }

    @Override
    public Response handle(Request request, NanoServer server) throws Exception {
        DataRepositoryView reg = server.getRegistry();

        Pair<Workspace, ? extends Dataset> p = findWorkspaceOrDataset(request, reg);

        StringWriter out = new StringWriter();
        JeoJSONWriter writer = new JeoJSONWriter(out);

        if (p == null) {
            // dump root
            handleAll(reg, writer);
        }
        else {
            try {
                if (p.second != null) {
                    handleDataset(p.second, writer, request);
                }
                else {
                    handleWorkspace(p.first, writer);
                }
            }
            finally {
                close(p.first);
                close(p.second);
            }
        }

        writer.flush();
        return new Response(HTTP_OK, MIME_JSON, out.toString());
    }

    void handleAll(DataRepositoryView reg, GeoJSONWriter w) throws IOException {
        w.object();
        
        for (Handle<?> item : reg.list()) {
            w.key(item.name()).object();

            w.key("type");
            Class<?> t = item.type();
            if (Workspace.class.isAssignableFrom(t)) {
                w.value("workspace");
            }
            else if (Dataset.class.isAssignableFrom(t)) {
                // we represent the dataset as a workspace
                w.value("workspace");
            }
            else if (Style.class.isAssignableFrom(t)) {
                w.value("style");
            }
            else {
                w.nul();
            }

            Driver<?> drv = item.driver();
            w.key("driver").value(drv.name());

            w.endObject();

        }
        w.endObject();
    }

    void handleWorkspace(Workspace ws, JeoJSONWriter w) throws IOException{
        w.workspace(ws);
    }

    void handleDataset(Dataset ds, JeoJSONWriter w, Request req) throws IOException{
         if (ds instanceof VectorDataset) {
             handleVectorDataset((VectorDataset)ds, w, req);
         }
         else if (ds instanceof TileDataset){
             handleTileDataset((TileDataset)ds, w, req);
         }
         else if (ds instanceof RasterDataset){
             handleRasterDataset((RasterDataset) ds, w, req);
         }
    }

    void handleVectorDataset(VectorDataset ds, JeoJSONWriter w, Request req) throws IOException {
        w.dataset(ds, (Map) Collections.singletonMap("features", "/features/" + createPath(req) + ".json"));
    }

    void handleTileDataset(TileDataset ds, JeoJSONWriter w, Request req) throws IOException {
        //TODO: link to first tile?
        w.dataset(ds, (Map) Collections.singletonMap("tiles", "/tiles/" + createPath(req)));
    }

    void handleRasterDataset(RasterDataset ds, JeoJSONWriter w, Request req) throws IOException {
        w.dataset(ds);
    }

    void handleStyle(Style s, GeoJSONWriter w, Request req) throws IOException {
        w.object()
          .key("type").value("style")
          .key("style").value("/styles/" + createPath(req))
          .endObject();
    }

    void close(Object obj) {
        if (obj instanceof Disposable) {
            ((Disposable)obj).close();
        }
    }

}
