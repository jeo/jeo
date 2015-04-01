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

import com.vividsolutions.jts.geom.Envelope;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import io.jeo.data.DataRepositoryView;
import io.jeo.data.Dataset;
import io.jeo.data.Handle;
import io.jeo.data.Workspace;
import io.jeo.filter.Filter;
import io.jeo.filter.cql.CQL;
import io.jeo.filter.cql.ParseException;
import io.jeo.map.MapBuilder;
import io.jeo.map.Style;
import io.jeo.map.View;
import io.jeo.render.Renderer;
import io.jeo.render.RendererFactory;
import io.jeo.render.Renderers;
import static io.jeo.nano.NanoHTTPD.HTTP_OK;
import io.jeo.proj.Proj;
import io.jeo.util.XMLWriter;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.osgeo.proj4j.units.Units;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for WMS 1.3.0 GetMap and GetCapabilities requests.
 */
public class WMSHandler extends OWSHandler {

    static final Logger LOG = LoggerFactory.getLogger(NanoServer.class);

    public WMSHandler() {
        super("wms");
    }

    @Override
    protected DelegateHandler resolveRequestHandler(String request, Request req) {
        DelegateHandler impl = null;
        
        if (request.equals("getcapabilities") || request.equals("capabilities")) {
            impl = new GetCaps();
        } else if (request.equals("getmap")) {
            impl = new GetMap(req);
        }

        return impl;
    }

    // for testing
    NanoHTTPD.Response render(RendererFactory factory, List<Dataset> dataSet, List<Style> styles,
            CoordinateReferenceSystem crs, Envelope bbox, int width, int height,
            String mimeType, List<Filter> filters) throws IOException {
        MapBuilder mb = new MapBuilder();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        mb.bounds(bbox).crs(crs).size(width, height);
        for (int i = 0; i < dataSet.size(); i++) {
            Filter filter = i < filters.size() ? filters.get(i) : null;
            mb.layer(dataSet.get(i), filter);
        }
        mb.style(Style.combine(styles));
        View view = mb.view();
        Renderer renderer = factory.create(view, null);
        renderer.init(view, null);
        try {
            renderer.render(bout);
        } finally {
            renderer.close();
            mb.map().close();
        }

        return new NanoHTTPD.Response(HTTP_OK, mimeType, new ByteArrayInputStream(bout.toByteArray()));
    }

    class GetCaps implements DelegateHandler {

        final XMLWriter xml;
        final List<Handle<Workspace>> workspaces;
        final List<Handle<Style>> styles;
        final StringWriter writer;

        GetCaps() {
            workspaces = new ArrayList<Handle<Workspace>>();
            styles = new ArrayList<Handle<Style>>();
            writer = new StringWriter();
            xml = new XMLWriter(writer);
        }

        @Override
        public NanoHTTPD.Response handle(Request req, NanoServer server) throws Exception {
            Iterable<Handle<?>> list = server.getRegistry().list();
            for (Handle h : list) {
                if (h.type() == Workspace.class) {
                    workspaces.add(h);
                } else if (h.type() == Style.class) {
                    styles.add(h);
                }
            }

            xml.start("WMS_Capabilities",
                    "version", "1.3.0", // @todo
                    "xmlns", "http://www.opengis.net/wms",
                    "xmlns:xlink", "http://www.w3.org/1999/xlink",
                    "xmlns:xsi","http://www.w3.org/2001/XMLSchema-instance",
                    "xsi:schemaLocation", "http://www.opengis.net/wms http://schemas.opengis.net/wms/1.3.0/capabilities_1_3_0.xsd"
                    );
            writeService();
            writeCapabilities(req, server);
            xml.end("WMS_Capabilities");
            xml.close();
            return new NanoHTTPD.Response(HTTP_OK, "text/xml", writer.toString());
        }

        private void writeService() {
            xml.start("Service");
            xml.element("Name", "WMS");
            xml.element("Title", "WMS");
            xml.element("OnlineResource", null, "xlink:type", "simple", "xlink:href", "http://www.opengis.net/wms");
            xml.end("Service");
        }

        private void writeCapabilities(Request req, NanoServer server) {
            xml.start("Capability");

            xml.start("Request");
            xml.start("GetCapabilities");
            // @todo - needed?
            xml.end("GetCapabilities");
            xml.start("GetMap");
            for (String mimeType: Renderers.listFormatMimeTypes(server.getRendererRegistry())) {
                xml.element("Format", mimeType);
            }
            xml.start("DCPType");
            xml.start("HTTP");
            xml.start("Get");
            xml.element("OnlineResource", null, "xlink:type", "simple", "xlink:href", "http://"+req.baseURL()+"/wms?SERVICE=WMS&");
            xml.end("Get");
            xml.end("HTTP");
            xml.end("DCPType");
            xml.end("GetMap");
            xml.end("Request");

            xml.start("Exception");
            xml.element("Format", "XML");
            xml.end("Exception");

            writeTopLevelLayer();

            xml.end("Capability");
        }

        private void writeTopLevelLayer() {
            xml.start("Layer");
            xml.element("Title", "WMS");
            writeCRS();
            writeBoundingBox("CRS:84", -180, 180, -90, 90);
            writeLatLonBoundingBox(new Envelope(-180, 180, -90, 90));
            writeStyles();
            writeLayers();
            xml.end("Layer");
        }

        private void writeCRS() {
            // some known ones
            // @todo crawl Proj to figure these out
            Set<Integer> all = new HashSet<Integer>();
            all.add(4326);
            all.add(900913);
            all.add(3857);
            for (Handle<Workspace> w : workspaces) {
                try {
                    for (Handle<Dataset> entry : w.resolve().list()) {
                        CoordinateReferenceSystem crs = entry.crs();
                        if (crs != null) {
                            Integer epsgCode = Proj.epsgCode(crs);
                            if (epsgCode != null) {
                                all.add(epsgCode);
                            }
                        } else {
                            LOG.warn(w.name() + ":" + entry.name() + " is missing crs");
                        }
                    }
                } catch (IOException ex) {
                    LOG.error("Error listing workspace " + w.name(), ex);
                }
            }
            for (Integer code : all) {
                xml.element("CRS", "EPSG:" + code);
            }
            xml.element("CRS", "CRS:84");
        }

        private void writeBoundingBox(String srs, double x1, double x2, double y1, double y2) {
            xml.element("BoundingBox", null, "SRS", srs, "minx", x1, "miny", y1, "maxx", x2, "maxy", y2);
        }

        private void writeLayers() {
            for (Handle<Workspace> w : workspaces) {
                try {
                    for (Handle<Dataset> entry : w.resolve().list()) {
                        writeLayer(w, entry);
                    }
                } catch (IOException ex) {
                    LOG.error("Error listing workspace " + w.name(), ex);
                }
            }
        }

        private void writeStyles() {
            for (Handle<Style> style : styles) {
                xml.start("Style");
                xml.element("Name", style.name());
                xml.end("Style");
            }
        }

        private void writeLayer(Handle<Workspace> ws, Handle<Dataset> lyr) throws IOException {
            xml.start("Layer", "queryable", "1");
            xml.element("Name", ws.name() + ":" + lyr.name());

            CoordinateReferenceSystem crs;
            try {
                crs = lyr.crs();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            if (crs == null) {
                crs = Proj.EPSG_4326; // @todo better than nothing ?
                LOG.warn(ws.name() + ":" + lyr.name() + " is missing crs");
            }
            String epsgCode = "EPSG:" + Proj.epsgCode(crs);
            xml.element("CRS", epsgCode);
            xml.element("CRS", "CRS:84");
            Envelope bbox = lyr.bounds();
            if (bbox == null) {
                // @todo another good guess?
                bbox = new Envelope(-180,180,-90,90);
                LOG.warn(ws.name() + ":" + lyr.name() + " is missing bbox");
            }

            Envelope lonLat =Proj.reproject(bbox, crs, Proj.EPSG_4326);

            writeBoundingBox("CRS:84", lonLat.getMinX(), lonLat.getMaxX(), lonLat.getMinY(), lonLat.getMaxY());
            // yay, the flipped bbox
            writeBoundingBox(epsgCode, bbox.getMinY(), bbox.getMaxY(), bbox.getMinX(), bbox.getMaxX());
            
            writeLatLonBoundingBox(lonLat);
            xml.end("Layer");
        }

        private String first(String... s)  {
            String f = null;
            for (int i = 0; i < s.length; i++) {
                if (s[i] != null) { f = s[i]; break; }
            }
            return f;
        }

        private void writeLatLonBoundingBox(Envelope bbox) {
            xml.start("EX_GeographicBoundingBox");
            xml.element("westBoundLongitude", bbox.getMinX());
            xml.element("eastBoundLongitude", bbox.getMaxX());
            xml.element("southBoundLatitude", bbox.getMinY());
            xml.element("northBoundLatitude", bbox.getMaxY());
            xml.end("EX_GeographicBoundingBox");
        }

    }

    class GetMap extends RequestParser implements DelegateHandler {

        GetMap(Request req) {
            super(req);
        }

        @Override
        public NanoHTTPD.Response handle(Request req, NanoServer server) throws Exception {
            this.parms = req.parms;
            String version = getParameter("version", true);
            if (!"1.3.0".equals(version)) {
                addError("No support for version: '" + version + "'");
            }
            String[] layerSpecs = getList("layers", true);
            String[] styleSpecs = getList("styles", false);
            Integer width = getInteger("width", true);
            Integer height = getInteger("height", true);
            String format = getParameter("format", "image/png");
            String filterSpec = getParameter("cql_filter", false);
            CoordinateReferenceSystem crs = getCRS();
            Envelope bbox = null;
            List<Filter> filters = new ArrayList<Filter>(3);

            if (crs != null) {
                // if geographic, flip axis parsing
                boolean flippedAxis = Units.DEGREES.name.equals(crs.getProjection().getUnits().name);
                bbox = getBBox(flippedAxis);
            }
            // @todo transparency + bgcolor

            Iterator<RendererFactory<?>> it = Renderers.listForFormat(format, server.getRendererRegistry());
            if (!it.hasNext()) {
                addError("No support for format: '" + format + "'");
            }

            List<Dataset> datasets = resolveDatasets(layerSpecs, server.getRegistry());
            List<Style> styles = resolveStyles(styleSpecs, datasets, server.getRegistry());

            if (crs == null && !datasets.isEmpty()) {
                crs = datasets.get(0).crs();
            }

            if (filterSpec != null) {
                String[] parts = filterSpec.split(";");
                if (parts.length > datasets.size()) {
                    addError(parts.length + " filters provided but only " + datasets.size() + " layers");
                }
                for (int i = 0; i < parts.length; i++) {
                    try {
                        Filter f = parts[i].length() > 0 ? CQL.parse(parts[i]) : null;
                        filters.add(f);
                    } catch (ParseException pe) {
                        addError("Invalid filter specifier [" + (i+1) + "] : " + pe.getMessage());
                    }
                }
            }

            NanoHTTPD.Response resp;
            if (errors == null) {
                 resp = render(it.next(), datasets, styles, crs, bbox, width, height, format, filters);
            } else {
                StringBuilder sb = new StringBuilder();
                for (String e : errors) {
                    sb.append(e).append('\n');
                }
                throw exception("MissingOrInvalidParameter", "request", sb.toString());
            }
            return resp;
        }

        private List<Dataset> resolveDatasets(String[] layerSpecs, DataRepositoryView registry) throws IOException {
            if (layerSpecs == null) layerSpecs = new String[0];
            List<Dataset> dataSets = new ArrayList<Dataset>(layerSpecs.length);
            for (String spec : layerSpecs) {
                Dataset resolved = resolve(registry, spec, this);
                if (resolved != null) {
                    dataSets.add(resolved);
                }
            }
            return dataSets;
        }

        private List<Style> resolveStyles(String[] styleSpecs, List<Dataset> datasets, DataRepositoryView registry) throws IOException {
            if (styleSpecs == null) styleSpecs = new String[0];
            List<Style> styles = new ArrayList<Style>(styleSpecs.length);
            for (String spec : styleSpecs) {
                Style style = registry.get(spec, Style.class);
                if (style == null) {
                    addError("No style : " + spec);
                } else {
                    styles.add(style);
                }
            }
            if (styles.isEmpty()) {
                styles.add(createStyle());
            }
            return styles;
        }

        private CoordinateReferenceSystem getCRS() {
            CoordinateReferenceSystem crs = null;
            String spec = getParameter("srs", getParameter("crs", false));
            if (spec == null) {
                missingParameter("srs");
            } else {
                crs = Proj.crs(spec);
                if (crs == null) {
                    addError("Invalid SRS specifier : " + spec);
                }
            }
            return crs;
        }

    }
}
