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
package io.jeo.json;

import com.vividsolutions.jts.geom.Envelope;
import io.jeo.data.Dataset;
import io.jeo.data.Driver;
import io.jeo.data.Handle;
import io.jeo.data.Workspace;
import io.jeo.geom.Envelopes;
import io.jeo.raster.Band;
import io.jeo.raster.RasterDataset;
import io.jeo.tile.TileDataset;
import io.jeo.tile.TileGrid;
import io.jeo.tile.TilePyramid;
import io.jeo.util.Key;
import io.jeo.util.Messages;
import io.jeo.vector.Field;
import io.jeo.geojson.GeoJSONWriter;
import io.jeo.util.Dimension;
import io.jeo.vector.VectorQuery;
import io.jeo.vector.VectorDataset;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import static java.lang.String.format;

/**
 * Writes common jeo objects as JSON.
 * <p>
 * Example:
 * <pre><code>
 * Writer w = ...;
 * Dataset data = ...;
 *
 * JeoJSONWriter writer = new JeoJSONWriter(w);
 * writer.object()
 * writer.key("data").dataset(data)
 * writer.endObject();
 * </code></pre>
 * </p>
 * @author Justin Deoliveira, Boundless
 */
public class JeoJSONWriter extends GeoJSONWriter {

    /**
     * Creates a new writer.
     *
     * @param out The writer to encode to.
     */
    public JeoJSONWriter(Writer out) {
        super(out);
    }

    /**
     * Creates a new writer with formatting.
     *
     * @param out The writer to encode to.
     * @param indentSize The number of spaces to use when indenting.
     */
    public JeoJSONWriter(Writer out, int indentSize) {
        super(out, indentSize);
    }

    /**
     * Encodes a driver object.
     */
    public JeoJSONWriter driver(Driver<?> drv, Map<String,Object> extra) throws IOException {
        object();

        key("name").value(drv.name());

        Messages msgs = new Messages();
        key("enabled").value(drv.isEnabled(msgs));

        key("aliases").array();
        for (String s : drv.aliases()) {
            value(s);
        }
        endArray();

        List<Throwable> errors = msgs.list();
        if (!errors.isEmpty()) {
            key("messages").array();
            for (Throwable t : errors) {
                value(format(Locale.ROOT, "%s: %s", t.getClass().getName(), t.getMessage()));
            }
            endArray();
        }

        key("type");
        Class<?> type = drv.type();
        if (Workspace.class.isAssignableFrom(type)) {
            value("workspace");
        }
        else if (Dataset.class.isAssignableFrom(type)) {
            value("dataset");
        }
        else {
            value(drv.type().getSimpleName());
        }

        key("keys").object();
        for (Key<?> key : drv.keys()) {
            key(key.name()).object();
            key("type").value(key.type().getSimpleName());
            if (key.def() != null) {
                key("default").value(key.def());
            }
            endObject();
        }
        endObject(); // keys

        encode(extra);
        endObject(); // root

        return this;
    }

    /**
     * Encodes a workspace object.
     */
    public JeoJSONWriter workspace(Workspace ws) throws IOException {
        object();

        key("type").value("workspace");
        key("driver").value(ws.driver().name());

        key("datasets").array();
        for (Handle<Dataset> ref : ws.list()) {
            value(ref.name());
        }
        endArray();

        return endObject();
    }

    /**
     * Encodes a Dataset object.
     */
    public JeoJSONWriter dataset(Dataset ds) throws IOException {
        if (ds instanceof VectorDataset) {
            dataset((VectorDataset)ds);
        }
        else if (ds instanceof RasterDataset) {
            dataset((RasterDataset)ds);
        }
        else if (ds instanceof TileDataset) {
            dataset((TileDataset)ds);
        }
        else {
            object().encode(ds).endObject();
        }
        return this;
    }

    /**
     * Encodes a VectorDataset object.
     */
    public JeoJSONWriter dataset(VectorDataset vds) throws IOException {
        return dataset(vds, null);
    }

    /**
     * Encodes a VectorDataset object.
     * <p>
     * The <tt>extra</tt> parameter can be used to encode extra attributes. It may be
     * <tt>null</tt>.
     * </p>
     */
    public JeoJSONWriter dataset(VectorDataset vds, Map<String,Object> extra) throws IOException {
        object();
        encode(vds);

        key("count").value(vds.count(new VectorQuery()));

        key("schema").object();
        for (Field fld : vds.schema()) {
            key(fld.name()).value(fld.type().getSimpleName());
        }
        endObject();

        encode(extra);
        return endObject();
    }

    /**
     * Encodes a RasterDataset object.
     */
    public JeoJSONWriter dataset(RasterDataset rds) throws IOException {
        return dataset(rds, null);
    }

    /**
     * Encodes a RasterDataset object.
     * <p>
     * The <tt>extra</tt> parameter can be used to encode extra attributes. It may be
     * <tt>null</tt>.
     * </p>
     */
    public JeoJSONWriter dataset(RasterDataset rds, Map<String,Object> extra) throws IOException {
        object();

        encode(rds);

        Dimension size = rds.size();
        key("size").array().value(size.width()).value(size.height()).endArray();

        key("bands").array();
        for (Band band : rds.bands()) {
            object();
            key("name").value(band.name());
            key("type").value(band.datatype());
            key("color").value(band.color());
            if (band.nodata() != null) {
                key("nodata").value(band.nodata());
            }
            endObject();
        }
        endArray();

        encode(extra);
        return endObject();
    }

    /**
     * Encodes a TileDataset object.
     */
    public JeoJSONWriter dataset(TileDataset tds) throws IOException {
        return dataset(tds, null);
    }

    /**
     * Encodes a TileDataset object.
     * <p>
     * The <tt>extra</tt> parameter can be used to encode extra attributes. It may be
     * <tt>null</tt>.
     * </p>
     */
    public JeoJSONWriter dataset(TileDataset tds, Map<String,Object> extra) throws IOException {
        object();

        encode(tds);

        TilePyramid pyr = tds.pyramid();
        key("tilesize").array().value(pyr.tileWidth())
                .value(pyr.tileHeight()).endArray();

        key("grids").array();
        for (TileGrid grid : tds.pyramid().grids()) {
            object().key("zoom").value(grid.z()).key("width")
                .value(grid.width()).key("height")
                .value(grid.height()).key("res").array()
                .value(grid.xres()).value(grid.yres()).endArray()
                .endObject();
        }
        endArray();

        encode(extra);
        return endObject();

    }

    /**
     * Helper to encode common Dataset attributes.
     */
    JeoJSONWriter encode(Dataset ds) throws IOException {
        key("name").value(ds.name());

        key("type");
        if (ds instanceof VectorDataset) {
            value("vector");
        }
        else if (ds instanceof TileDataset ){
            value("tile");
        }
        else if (ds instanceof RasterDataset) {
            value("raster");
        }
        else {
            nul();
        }

        key("driver").value(ds.driver().name());

        Envelope bbox = ds.bounds();
        if (!Envelopes.isNull(bbox)) {
            key("bbox");
            bbox(bbox);
        }

        CoordinateReferenceSystem crs = ds.crs();
        if (crs != null) {
            key("crs");
            array();

            if (crs.getParameters() != null) {
                for (String s : crs.getParameters()) {
                    value(s);
                }
            }
            endArray();
        }

        return this;
    }

    /**
     * Helper to encode a map of key value pairs recursively.
     */
    JeoJSONWriter encode(Map<String,Object> map) throws IOException {
        if (map == null) {
            return this;
        }

        for (Map.Entry<String,Object> e : map.entrySet()) {
            key(e.getKey());

            if (e.getValue() instanceof Map) {
                object();
                encode((Map<String,Object>)e.getValue());
                endObject();
            }
            else {
                value(e.getValue());
            }
        }

        return this;
    }

    //overrides for typing narrowing
    @Override
    public JeoJSONWriter object() throws IOException {
        return (JeoJSONWriter) super.object();
    }

    @Override
    public JeoJSONWriter array() throws IOException {
        return (JeoJSONWriter) super.array();
    }

    @Override
    public JeoJSONWriter key(String key) throws IOException {
        return (JeoJSONWriter) super.key(key);
    }

    @Override
    public JeoJSONWriter value(Number value) throws IOException {
        return (JeoJSONWriter) super.value(value);
    }

    @Override
    public JeoJSONWriter value(Object value) throws IOException {
        return (JeoJSONWriter) super.value(value);
    }

    @Override
    public JeoJSONWriter nul() throws IOException {
        return (JeoJSONWriter) super.nul();
    }

    @Override
    public JeoJSONWriter value(String value) throws IOException {
        return (JeoJSONWriter) super.value(value);
    }

    @Override
    public JeoJSONWriter value(double value) throws IOException {
        return (JeoJSONWriter) super.value(value);
    }

    @Override
    public JeoJSONWriter value(long value) throws IOException {
        return (JeoJSONWriter) super.value(value);
    }

    @Override
    public JeoJSONWriter endObject() throws IOException {
        return (JeoJSONWriter) super.endObject();
    }

    @Override
    public JeoJSONWriter endArray() throws IOException {
        return (JeoJSONWriter) super.endArray();
    }

    @Override
    public JeoJSONWriter flush() throws IOException {
        return (JeoJSONWriter) super.flush();
    }

}
