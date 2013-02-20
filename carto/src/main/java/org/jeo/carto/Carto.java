package org.jeo.carto;

public class Carto {

    static class Element {
        public final String key;

        Element(String key) {
            this.key = key;
        }

        public Object get(java.util.Map<String,Object> map) {
            return map.get(key);
        }

        public void set(java.util.Map<String, Object> map, Object value) {
            map.put(key, value);
        }
    }

    public static class Map {
        public static final Element BACKGROUND_COLOR = new Element("background-color");
        public static final Element BACKGROUND_IMAGE = new Element("background-image");
        public static final Element SRS = new Element("srs");
        public static final Element BUFFER_SIZE = new Element("buffer-sise");
        public static final Element BASE = new Element("base");
        public static final Element FONT_DIRECTORY = new Element("font-directory");

    }

    public static class Polygon {
        public static final Element FILL = new Element("polygon-color");
        public static final Element OPACITY = new Element("polygon-opacity");
        public static final Element GAMMA = new Element("polygon-gamma");
        public static final Element GAMMA_METHOD = new Element("polygon-gamma-method");
        public static final Element CLIP = new Element("polygon-clip");
        public static final Element SMOOTH = new Element("polygon-smooth");
        public static final Element COMP_OP = new Element("polygon-comp-op");
    }

    public static class Line {
        public static final Element COLOR = new Element("line-color");
        public static final Element WIDTH = new Element("line-width");
        public static final Element OPACITY = new Element("line-opacity");
        public static final Element JOIN = new Element("line-join");
        public static final Element CAP = new Element("line-cap");
        public static final Element GAMMA = new Element("line-gamma");
        public static final Element GAMMA_METHOD = new Element("line-gamma-method");
        public static final Element DASHARRAY = new Element("line-dasharray");
        public static final Element DASH_OFFSET = new Element("line-dash-offset");
        public static final Element MITERLIMIT = new Element("line-miterlimit");
        public static final Element CLIP = new Element("line-clip");
        public static final Element SMOOTH = new Element("line-smooth");
        public static final Element OFFSET = new Element("line-offset");
        public static final Element RASTERIZER = new Element("line-rasterizer");
        public static final Element COMP_OP = new Element("line-comp-op");
    }

    public static class Marker {
        public static final Element FILE = new Element("marker-file");
        public static final Element OPACITY = new Element("marker-opacity");
        public static final Element FILL_OPACITY = new Element("marker-fill-opacity");
        public static final Element LINE_COLOR = new Element("marker-line-color");
        public static final Element LINE_WIDTH = new Element("marker-line-width");
        public static final Element LINE_OPACITY = new Element("marker-line-opacity");
        public static final Element PLACEMENT = new Element("marker-placement");
        public static final Element TYPE = new Element("marker-type");
        public static final Element WIDTH = new Element("marker-width");
        public static final Element HEIGHT = new Element("marker-height");
        public static final Element FILL = new Element("marker-fill");
        public static final Element ALLOW_OVERLAP = new Element("marker-allow-overlap");
        public static final Element IGNORE_PLACEMENT = new Element("marker-ignore-placement");
        public static final Element SPACING = new Element("marker-spacing");
        public static final Element MAX_ERROR = new Element("marker-max-error");
        public static final Element TRANSFORM = new Element("marker-transform");
        public static final Element CLIP = new Element("marker-clip");
        public static final Element SMOOTH = new Element("marker-smooth");
        public static final Element COMP_OP = new Element("marker-comp-op");
    }

    public static class Shield {
        public static final Element NAME = new Element("shield-name");
        public static final Element FILE = new Element("shield-file");
        public static final Element FACE_NAME = new Element("shield-face-name");
        public static final Element SIZE = new Element("shield-size");
        public static final Element FILL = new Element("shield-fill");
        public static final Element PLACEMENT = new Element("shield-placement");
        public static final Element AVOID_EDGES = new Element("shield-avoid-edges");
        public static final Element ALLOW_OVERLAP = new Element("shield-allow-overlap");
        public static final Element MIN_DISTANCE = new Element("shield-min-distance");
        public static final Element SPACING = new Element("shield-spacing");
        public static final Element MIN_PADDING = new Element("shield-min-padding");
        public static final Element WRAP_WIDTH = new Element("shield-wrap-width");
        public static final Element WRAP_BEFORE = new Element("shield-wrap-before");
        public static final Element WRAP_CHARACTER = new Element("shield-wrap-character");
        public static final Element HALO_FILL = new Element("shield-halo-fill");
        public static final Element HALO_RADIUS = new Element("shield-halo-radius");
        public static final Element CHARACTER_SPACING = new Element("shield-character-spacing");
        public static final Element LINE_SPACING = new Element("shield-line-spacing");
        public static final Element TEXT_DX = new Element("shield-text-dx");
        public static final Element TEXT_DY = new Element("shield-text-dy");
        public static final Element OPACITY = new Element("shield-opacity");
        public static final Element TEXT_OPACITY = new Element("shield-text-opacity");
        public static final Element HORIZONAL_ALIGNMENT = new Element("shield-horizontal-alignment");
        public static final Element VERTICAL_ALIGNMENT = new Element("shield-vertical-alignment");
        public static final Element TEXT_TRANSFORM = new Element("shield-text-transform");
        public static final Element JUSTIFY_ALIGNMENT = new Element("shield-justify-alignment");
        public static final Element CLIP = new Element("shield-clip");
        public static final Element COMP_OP = new Element("shield-comp-op");
    }

    public static class LinePattern {
        public static final Element FILE = new Element("line-pattern-file");
        public static final Element CLIP = new Element("line-pattern-clip");
        public static final Element SMOOTH = new Element("line-pattern-smooth");
        public static final Element COMP_OP = new Element("line-pattern-comp-op");
    }

    public static class PolygonPattern {
        public static final Element FILE = new Element("polygon-pattern-file");
        public static final Element ALIGNMNET = new Element("polygon-pattern-alignment");
        public static final Element GAMMA = new Element("polygon-pattern-gamma");
        public static final Element OPACITY = new Element("polygon-pattern-opacity");
        public static final Element CLIP = new Element("polygon-pattern-clip");
        public static final Element SMOOTH = new Element("polygon-pattern-smooth");
        public static final Element COMP_OP = new Element("polygon-pattern-comp-op");
    }

    public static class Raster {
        public static final Element OPACITY = new Element("raster-opacity");
        public static final Element FILTER_FACTOR = new Element("raster-filter-factor");
        public static final Element SCALING = new Element("raster-scaling");
        public static final Element MESH_SIZE = new Element("raster-mesh-size");
        public static final Element COMP_OP = new Element("raster-comp-op");
    }

    public static class Point {
        public static final Element FILE = new Element("point-file");
        public static final Element ALLOW_OVERLAP = new Element("point-overlap");
        public static final Element IGNORE_PLACEMENT = new Element("point-ignore-placement");
        public static final Element OPACITY = new Element("point-opacity");
        public static final Element PLACEMENT = new Element("point-placement");
        public static final Element TRANSFORM = new Element("point-transform");
        public static final Element COMP_OP = new Element("point-comp-op");
    }

    public static class Text {
        public static final Element NAME = new Element("text-name");
        public static final Element FACE_NAME = new Element("text-face-name");
        public static final Element SIZE = new Element("text-size");
        public static final Element RATIO = new Element("text-ratio");
        public static final Element WRAP_WIDTH = new Element("text-width");
        public static final Element WRAP_BEFORE = new Element("text-before");
        public static final Element WRAP_CHARACTER = new Element("text-character");
        public static final Element SPACING = new Element("text-spacing");
        public static final Element CHARACTER_SPACING = new Element("text-character-spacing");
        public static final Element LINE_SPACING = new Element("text-line-spacing");
        public static final Element LABEL_POSITION_TOLERANCE = new Element("text-label-position-tolerance");
        public static final Element MAX_CHAR_ANGLE_DELTA = new Element("text-max-char-angle-delta");
        public static final Element FILL = new Element("text-fill");
        public static final Element OPACITY = new Element("text-opacity");
        public static final Element HALO_FILL = new Element("text-halo-fill");
        public static final Element HALO_RADIUS = new Element("text-radius");
        public static final Element DX = new Element("text-dx");
        public static final Element DY = new Element("text-dy");
        public static final Element VERTICAL_ALIGNMENT = new Element("text-vertical-alignment");
        public static final Element AVOID_EDGES = new Element("text-avoid-edges");
        public static final Element MIN_DISTANCE = new Element("text-min-distance");
        public static final Element MIN_PADDING = new Element("text-min-padding");
        public static final Element MIN_PATH_LENGTH = new Element("text-min-path-length");
        public static final Element ALLOW_OVERLAP = new Element("text-allow-overlap");
        public static final Element ORIENTATION = new Element("text-orientiation");
        public static final Element PLACEMENT = new Element("text-placement");
        public static final Element PLACEMENT_TYPE = new Element("text-placement-type");
        public static final Element PLACEMENTS = new Element("text-placements");
        public static final Element TRANSFORM = new Element("text-transform");
        public static final Element HORIZONTAL_ALIGNMENT = new Element("text-horizontal-alignment");
        public static final Element ALIGN = new Element("text-align");
        public static final Element CLIP = new Element("text-clip");
        public static final Element COMP_OP = new Element("text-comp-op");
    }

    public static class Building {
        public static final Element FILL = new Element("building-fill");
        public static final Element FILL_OPACITY = new Element("building-fill-opacity");
        public static final Element HEIGHT = new Element("building-height");
    }
}
