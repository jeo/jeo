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
package org.jeo.json;

import com.vividsolutions.jts.geom.Envelope;
import org.jeo.data.*;
import org.jeo.raster.RasterDataset;
import org.jeo.tile.TileDataset;
import org.jeo.vector.Field;
import org.jeo.geojson.GeoJSONWriter;
import org.jeo.geom.Envelopes;
import org.jeo.raster.Band;
import org.jeo.tile.TileGrid;
import org.jeo.tile.TilePyramid;
import org.jeo.util.Dimension;
import org.jeo.vector.Query;
import org.jeo.vector.VectorDataset;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

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
     * Encodes a workspace object.
     */
    public JeoJSONWriter workspace(Workspace ws) throws IOException {
        object();

        key("type").value("workspace");
        key("driver").value(ws.getDriver().getName());

        key("datasets").array();
        for (Handle<Dataset> ref : ws.list()) {
            value(ref.getName());
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

        key("count").value(vds.count(new Query()));

        key("schema").object();
        for (Field fld : vds.schema()) {
            key(fld.getName()).value(fld.getType().getSimpleName());
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
        key("tilesize").array().value(pyr.getTileWidth())
                .value(pyr.getTileHeight()).endArray();

        key("grids").array();
        for (TileGrid grid : tds.pyramid().getGrids()) {
            object().key("zoom").value(grid.getZ()).key("width")
                .value(grid.getWidth()).key("height")
                .value(grid.getHeight()).key("res").array()
                .value(grid.getXRes()).value(grid.getYRes()).endArray()
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
        key("name").value(ds.getName());

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

        key("driver").value(ds.getDriver().getName());

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
