/* Copyright 2014 The jeo project. All rights reserved.
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
package org.jeo.cli.cmd;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Iterator;
import java.util.List;

import org.jeo.cli.JeoCLI;
import org.jeo.data.Drivers;
import org.jeo.data.VectorDataset;
import org.jeo.geojson.GeoJSONWriter;
import org.jeo.map.Map;
import org.jeo.map.MapBuilder;
import org.jeo.map.Style;
import org.jeo.map.View;
import org.jeo.render.Renderer;
import org.jeo.render.RendererFactory;
import org.jeo.render.Renderers;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.internal.Maps;
import com.vividsolutions.jts.geom.Envelope;

@Parameters(commandNames="render", commandDescription="Renders a data set")
public class RenderCmd extends JeoCmd {

    @Parameter(description="dataset", arity = 1, required=true)
    List<String> datas;

    @Parameter(names = {"-s", "--style"}, description = "Style to use for rendering", required=true)
    Style style;

    @Parameter(names = {"-r", "--renderer"}, description = "Renderer name", required=true)
    String renderer;

    @Parameter(names = { "-o", "--opts"}, description = "Rendering options")
    java.util.Map opts = Maps.newLinkedHashMap();

    @Parameter(names = { "-l", "--list"}, description = "Lists available renderers", help=true)
    boolean list = false;

    @Parameter(names = {"-b", "--bbox"}, description = "Bounding box (xmin,ymin,xmax,ymax)")
    Envelope bbox;

    @Parameter(names = {"-d", "--dimensions"}, description = "Rendering dimensions (width,height)")
    Dimension size = new Dimension(256, 256);

    @Parameter(names = {"-f", "--file"}, description = "File to render to")
    String file;

    @Override
    protected void doCommand(JeoCLI cli) throws Exception {
        if (list) {
            list(cli);
        }
        else {

            MapBuilder mb = Map.build();

            // layers
            for (String data : datas) {
                URI uri = parseDataURI(data);

                VectorDataset dataset = open(Drivers.open(uri, VectorDataset.class));
                if (dataset == null) {
                    throw new IllegalArgumentException("Unable to load dataset: " + data);
                }

                mb.layer(dataset);
            }

            mb.style(style);
            mb.size(size.width, size.height);
            if (bbox != null) {
                mb.bounds(bbox);
            }

            OutputStream out = file != null ? new FileOutputStream(new File(file)) : System.out;

            View view = mb.view();

            Renderer r = Renderers.create(renderer, view, opts);
            if (r == null) {
                throw new IllegalArgumentException(String.format(
                        "Unable to create renderer '%s' from options: %s", renderer, opts));
            }

            r.init(view, opts);
            r.render(out);
            out.flush();
            if (file != null) {
                out.close();
            }
            r.close();
        }
    }

     void list(JeoCLI cli) throws IOException {
         GeoJSONWriter w = cli.newJSONWriter();
         w.array();

         Iterator<RendererFactory<?>> it = Renderers.list();
         while(it.hasNext()) {
             RendererFactory<?> rf = it.next();

             w.object();
             w.key("name").value(rf.getName());
             if (!rf.getAliases().isEmpty()) {
                 w.key("aliases").array();
                 for (String a : rf.getAliases()) {
                     w.value(a);
                 }
                 w.endArray();
             }
             w.key("formats").array();
             for (String format : rf.getFormats()) {
                 w.value(format);
             }
             w.endArray();
             w.endObject();
         }

         w.endArray();
         w.flush();
    }

}
