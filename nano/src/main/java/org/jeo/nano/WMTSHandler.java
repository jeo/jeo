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

import com.vividsolutions.jts.geom.Envelope;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import org.jeo.data.Dataset;
import org.jeo.data.Handle;
import org.jeo.data.TileDataset;
import org.jeo.data.Workspace;
import static org.jeo.nano.NanoHTTPD.HTTP_NOTFOUND;
import static org.jeo.nano.NanoHTTPD.HTTP_OK;
import static org.jeo.nano.NanoHTTPD.MIME_PLAINTEXT;
import org.jeo.nano.NanoHTTPD.Response;
import static org.jeo.nano.OWSHandler.exception;
import org.jeo.proj.Proj;
import org.jeo.tile.Tile;
import org.jeo.tile.TileGrid;
import org.jeo.tile.TilePyramid;
import org.jeo.util.XMLWriter;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.osgeo.proj4j.units.Units;
import org.slf4j.LoggerFactory;

/**
 * Handler for WMTS 1.0.0 GetCapabilities and GetTile requests.
 */
public class WMTSHandler extends OWSHandler {
    
    static final org.slf4j.Logger LOG = LoggerFactory.getLogger(NanoServer.class);

    public WMTSHandler() {
        super("wmts");
    }

    @Override
    protected DelegateHandler resolveRequestHandler(String request, Request req) {
        DelegateHandler impl = null;

        if (request.equals("getcapabilities") || request.equals("capabilities")) {
            impl = new GetCaps();
        } else if (request.equals("gettile")) {
            impl = new GetTile(req);
        }

        return impl;
    }

    /**
     * Compute the scale denominator from pixelSpan for the given crs. This is
     * used to compute values for representing tiled data matrix sets. This
     * formula is described in the OGC WMTS 1.0.0 specification.
     *
     * @param pixelSpan The pixelSpan (units/pixel) in units of the crs
     * @param crs The non-null crs
     * @return scaleDenominator
     */
    public static double computeScaleDenominator(double pixelSpan, CoordinateReferenceSystem crs) {
        double metersPerUnit;
        if (crs.getProjection().getUnits().name.equals(Units.DEGREES.name)) {
            metersPerUnit = 1 / 111319.49079327357;
        } else {
            metersPerUnit = Units.convert(1, Units.METRES, crs.getProjection().getUnits());
        }
        return pixelSpan / .28e-3 / metersPerUnit;
    }

    private class GetCaps implements DelegateHandler {

        final XMLWriter xml;
        final List<Handle<Workspace>> workspaces;
        final StringWriter writer;
        String uri;

        GetCaps() {
            workspaces = new ArrayList<Handle<Workspace>>();
            writer = new StringWriter();
            xml = new XMLWriter(writer);
        }

        @Override
        public NanoHTTPD.Response handle(Request req, NanoServer server) throws Exception {
            uri = "http://" + req.baseURL() + "/" + serviceName;
            Iterable<Handle<?>> list = server.getRegistry().list();
            for (Handle h : list) {
                if (h.getType() == Workspace.class) {
                    workspaces.add(h);
                }
            }

            xml.start("Capabilities",
                    "version", "1.0.0",
                    "xmlns", "http://www.opengis.net/wmts/1.0",
                    "xmlns:ows", "http://www.opengis.net/ows/1.1",
                    "xmlns:xlink", "http://www.w3.org/1999/xlink",
                    "xmlns:xsi","http://www.w3.org/2001/XMLSchema-instance",
                    "xsi:schemaLocation", "http://www.opengis.net/wmts/1.0 http://schemas.opengis.net/wmts/1.0/wmtsGetCapabilities_response.xsd"
                    );
            writeServiceIdentification();
            writeServiceProvider();
            writeOperationsMetadata();
            writeContents();
            writeServiceMetadataURL();
            xml.end("Capabilities");
            xml.close();
            return new NanoHTTPD.Response(HTTP_OK, "text/xml", writer.toString());
        }

        private void writeServiceIdentification() {
            xml.start("ows:ServiceIdentification");
            xml.element("ows:Title", "jeo WMTS");
            xml.element("ows:ServiceType", "OGC WMTS");
            xml.element("ows:ServiceTypeVersion", "1.0.0");
            xml.end("ows:ServiceIdentification");

        }

        private void writeServiceProvider() {
            xml.start("ows:ServiceProvider");
            xml.element("ows:ProviderName", "jeo WMTS");
            xml.emptyElement("ows:ProviderSite", "xlink:href", uri);
            xml.start("ows:ServiceContact");
            xml.element("ows:IndividualName", "you");
            xml.end("ows:ServiceContact");
            xml.end("ows:ServiceProvider");

        }

        private void writeOperationsMetadata() {
            xml.start("ows:OperationsMetadata");
            writeOperation("GetCapabilities");
            writeOperation("GetTile");
            xml.end("ows:OperationsMetadata");
        }

        private void writeOperation(String capabilities) {
            xml.start("ows:Operation", "name", capabilities);
            xml.start("ows:DCP");
            xml.start("ows:HTTP");
            xml.start("ows:Get", "xlink:href", uri + "?");
            xml.start("ows:Constraint", "name", "GetEncoding");
            xml.start("ows:AllowedValues");
            xml.element("ows:Value", "KVP");
            xml.end("ows:AllowedValues");
            xml.end("ows:Constraint");
            xml.end("ows:Get");
            xml.end("ows:HTTP");
            xml.end("ows:DCP");
            xml.end("ows:Operation");
        }

        private void writeContents() throws IOException {
            xml.start("Contents");
            List<Object[]> tileDatasets = new ArrayList<Object[]>();
            for (Handle<Workspace> w: workspaces) {
                try {
                    for (Handle<Dataset> ds: w.resolve().list()) {
                        Dataset dataset = ds.resolve();
                        if (dataset instanceof TileDataset) {
                            String id = w.getName() + ":" + ds.getName();
                            Integer epsgCode = Proj.epsgCode(ds.crs());
                            if (epsgCode == null) {
                                LOG.warn("Skipping " + w.getName() + ":" + ds.getName() + ", no epsgCode resolved");
                                continue;
                            }
                            TileDataset tds = (TileDataset) dataset;
                            writeLayer(id, tds);
                            tileDatasets.add(new Object[] {id, epsgCode, tds});
                        }
                    }
                } catch (IOException ex) {
                    LOG.error("Error listing workspace " + w.getName(), ex);
                }
            }
            for (Object[] td: tileDatasets) {
                writeTileMatrix((String)td[0], (Integer)td[1], (TileDataset)td[2]);
            }
            xml.end("Contents");
        }

        private void writeServiceMetadataURL() {
            xml.emptyElement("ServiceMetadataURL", "xlink:href", uri);
        }

        private void writeLayer(String id, TileDataset ds) throws IOException {
            xml.start("Layer");

            xml.element("ows:Title", ds.getTitle());
            xml.element("ows:Abstract", ds.getDescription());
            Envelope bbox = Proj.reproject(ds.bounds(), ds.crs(), Proj.EPSG_4326);
            xml.start("ows:WGS84BoundingBox");
            xml.element("ows:LowerCorner", bbox.getMinX() + " " + bbox.getMinY());
            xml.element("ows:UpperCorner", bbox.getMaxX() + " " + bbox.getMaxY());
            xml.end("ows:WGS84BoundingBox");
            xml.element("ows:Identifier", id);
            xml.start("Style", "isDefault", true);
            xml.element("ows:Identifier", "Default");
            xml.end("Style");

            xml.element("Format", "image/png");
            xml.element("Format", "image/jpeg");

            writeLimits(ds, id);

            xml.end("Layer");
        }

        private void writeLimits(TileDataset ds, String id) throws IOException {
            xml.start("TileMatrixSetLink");
            xml.element("TileMatrixSet", id);
            xml.start("TileMatrixSetLimits");
            for (TileGrid tileGrid : ds.pyramid().getGrids()) {
                writeLimit(tileGrid);
            }
            xml.end("TileMatrixSetLimits");
            xml.end("TileMatrixSetLink");
        }

        private void writeLimit(TileGrid tileGrid) {
            xml.start("TileMatrixLimits");
            xml.element("TileMatrix", tileGrid.getZ());
            xml.element("MinTileRow", 0);
            xml.element("MaxTileRow", tileGrid.getHeight() - 1);
            xml.element("MinTileCol", 0);
            xml.element("MaxTileCol", tileGrid.getWidth() - 1);
            xml.end("TileMatrixLimits");
        }

        private void writeTileMatrix(String key, Integer epsgCode, TileDataset ds) throws IOException {
            xml.start("TileMatrixSet");
            xml.element("ows:Identifier", key);
            xml.element("ows:SupportedCRS", "urn:ogc:def:crs:EPSG::" + epsgCode);
            TilePyramid pyramid = ds.pyramid();
            for (TileGrid tileGrid : pyramid.getGrids()) {
                writeTileMatrix(pyramid, tileGrid, ds.crs());
            }
            xml.end("TileMatrixSet");
        }

        private void writeTileMatrix(TilePyramid pyramid, TileGrid tileGrid, CoordinateReferenceSystem crs) {
            xml.start("TileMatrix");
            xml.element("ows:Identifier", tileGrid.getZ());
            double sd = computeScaleDenominator(tileGrid.getXRes(), crs);
            xml.element("ScaleDenominator", String.format("%f", sd));

            // @todo do we need to right TopLeftCorner?
            xml.element("TileWidth", pyramid.getTileWidth());
            xml.element("TileHeight", pyramid.getTileHeight());
            xml.element("MatrixWidth", tileGrid.getWidth());
            xml.element("MatrixHeight", tileGrid.getHeight());
            xml.end("TileMatrix");
        }

    }

    private class GetTile extends RequestParser implements DelegateHandler {

        public GetTile(Request req) {
            super(req);
        }

        @Override
        public NanoHTTPD.Response handle(Request req, NanoServer server) throws Exception {
            NanoHTTPD.Response resp;
            String layerSpec = getParameter("layer", true);
            String format = getParameter("format", true);
            Integer tileMatrix = getInteger("tilematrix", true);
            String tileMatrixSet = getParameter("tilematrixset", true);
            Integer tileRow = getInteger("tilerow", true);
            Integer tileCol = getInteger("tilecol", true);
            TileDataset layer = null;
            TileGrid grid = null;

            if (layerSpec != null) {
                Dataset ds = resolve(server.getRegistry(), layerSpec, this);
                if (!(ds instanceof TileDataset)) {
                    addError("layer is not tiled : " + layerSpec);
                } else {
                    layer = (TileDataset) ds;
                    grid = layer.pyramid().grid(tileMatrix);
                    if (grid == null) {
                        addError("no tileMatrix at " + tileMatrix);
                    }
                }
            }

            if (errors == null) {
                // jeo tiles are from lower-left corner, not upper-right so invert the row number
                resp = render(layer, format, tileCol, grid.getHeight() - tileRow - 1, tileMatrix);
            } else {
                StringBuilder sb = new StringBuilder();
                for (String e : errors) {
                    sb.append(e).append('\n');
                }
                throw exception("MissingOrInvalidParameter", "request", sb.toString());
            }
            return resp;
        }

        private NanoHTTPD.Response render(TileDataset layer, String format, Integer tileCol, Integer tileRow, Integer zoom) throws IOException {
            Tile t = layer.read(zoom, tileCol, tileRow);
            if (t == null) {
                return new Response(HTTP_NOTFOUND, MIME_PLAINTEXT, String.format(
                    "No such tile z = %d, x = %d, y = %d", zoom, tileCol, tileRow));
            }

            return new Response(HTTP_OK, t.getMimeType(), new ByteArrayInputStream(t.getData()));
        }
    }

}
