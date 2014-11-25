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
package org.jeo.map;

/**
 * Defines style properties from the <a href=http://www.mapbox.com/carto/api/2.1.0/">CartoCSS</a> 
 * styling language. 
 *  
 * @author Justin Deoliveira, OpenGeo
 */
public interface CartoCSS {
    // all
    static final String COMP_OP = "comp-op";
    static final String IMAGE_FILTERS = "image-filters";
    static final String OPACITY = "opacity";
    
    // map
    static final String BACKGROUND_COLOR = "background-color";
    static final String BACKGROUND_IMAGE = "background-image";
    static final String BASE = "base";
    static final String BUFFER_SIZE = "buffer-size";
    static final String FONT_DIRECTORY = "font-directory";
    static final String SRS = "srs";

    // polygon
    static final String POLYGON_FILL = "polygon-fill";
    static final String POLYGON_OPACITY = "polygon-opacity";
    static final String POLYGON_GAMMA = "polygon-gamma";
    static final String POLYGON_GAMMA_METHOD = "polygon-gamma-method";
    static final String POLYGON_CLIP = "polygon-clip";
    static final String POLYGON_SMOOTH = "polygon-smooth";
    static final String POLYGON_COMP_OP = "polygon-comp-op";

    // line
    static final String LINE_COLOR = "line-color";
    static final String LINE_WIDTH = "line-width";
    static final String LINE_OPACITY = "line-opacity";
    static final String LINE_JOIN = "line-join";
    static final String LINE_CAP = "line-cap";
    static final String LINE_GAMMA = "line-gamma";
    static final String LINE_GAMMA_METHOD = "line-gamma-method";
    static final String LINE_DASHARRAY = "line-dasharray";
    static final String LINE_DASH_OFFSET = "line-dash-offset";
    static final String LINE_MITERLIMIT = "line-miterlimit";
    static final String LINE_CLIP = "line-clip";
    static final String LINE_SMOOTH = "line-smooth";
    static final String LINE_OFFSET = "line-offset";
    static final String LINE_RASTERIZER = "line-rasterizer";
    static final String LINE_COMP_OP = "line-comp-op";
    
    // marker 
    static final String MARKER_FILE = "marker-file";
    static final String MARKER_OPACITY = "marker-opacity";
    static final String MARKER_FILL_OPACITY = "marker-fill-opacity";
    static final String MARKER_LINE_COLOR = "marker-line-color";
    static final String MARKER_LINE_WIDTH = "marker-line-width";
    static final String MARKER_LINE_OPACITY = "marker-line-opacity";
    static final String MARKER_PLACEMENT = "marker-placement";
    static final String MARKER_TYPE = "marker-type";
    static final String MARKER_WIDTH = "marker-width";
    static final String MARKER_HEIGHT = "marker-height";
    static final String MARKER_FILL = "marker-fill";
    static final String MARKER_ALLOW_OVERLAP = "marker-allow-overlap";
    static final String MARKER_IGNORE_PLACEMENT = "marker-ignore-placement";
    static final String MARKER_SPACING = "marker-spacing";
    static final String MARKER_MAX_ERROR = "marker-max-error";
    static final String MARKER_TRANSFORM = "marker-transform";
    static final String MARKER_CLIP = "marker-clip";
    static final String MARKER_SMOOTH = "marker-smooth";
    static final String MARKER_COMP_OP = "marker-comp-op";

    // shield
    static final String SHIELD_NAME = "shield-name";
    static final String SHIELD_FILE = "shield-file";
    static final String SHIELD_FACE_NAME = "shield-face-name";
    static final String SHIELD_SIZE = "shield-size";
    static final String SHIELD_FILL = "shield-fill";
    static final String SHIELD_PLACEMENT = "shield-placement";
    static final String SHIELD_AVOID_EDGES = "shield-avoid-edges";
    static final String SHIELD_ALLOW_OVERLAP = "shield-allow-overlap";
    static final String SHIELD_MIN_DISTANCE = "shield-min-distance";
    static final String SHIELD_SPACING = "shield-spacing";
    static final String SHIELD_MIN_PADDING = "shield-min-padding";
    static final String SHIELD_WRAP_WIDTH = "shield-wrap-width";
    static final String SHIELD_WRAP_BEFORE = "shield-wrap-before";
    static final String SHIELD_WRAP_CHARACTER = "shield-wrap-character";
    static final String SHIELD_HALO_FILL = "shield-halo-fill";
    static final String SHIELD_HALO_RADIUS = "shield-halo-radius";
    static final String SHIELD_CHARACTER_SPACING = "shield-character-spacing";
    static final String SHIELD_LINE_SPACING = "shield-line-spacing";
    static final String SHIELD_TEXT_DX = "shield-text-dx";
    static final String SHIELD_TEXT_DY = "shield-text-dy";
    static final String SHIELD_OPACITY = "shield-opacity";
    static final String SHIELD_TEXT_OPACITY = "shield-text-opacity";
    static final String SHIELD_HORIZONAL_ALIGNMENT = "shield-horizontal-alignment";
    static final String SHIELD_VERTICAL_ALIGNMENT = "shield-vertical-alignment";
    static final String SHIELD_TEXT_TRANSFORM = "shield-text-transform";
    static final String SHIELD_JUSTIFY_ALIGNMENT = "shield-justify-alignment";
    static final String SHIELD_CLIP = "shield-clip";
    static final String SHIELD_COMP_OP = "shield-comp-op";
    
    // line pattern
    static final String LINE_PATTERN_FILE = "line-pattern-file";
    static final String LINE_PATTERN_CLIP = "line-pattern-clip";
    static final String LINE_PATTERN_SMOOTH = "line-pattern-smooth";
    static final String LINE_PATTERN_COMP_OP = "line-pattern-comp-op";

    // polygon-pattern
    static final String POLYGON_PATTERN_FILE = "polygon-pattern-file";
    static final String POLYGON_PATTERN_ALIGNMNET = "polygon-pattern-alignment";
    static final String POLYGON_PATTERN_GAMMA = "polygon-pattern-gamma";
    static final String POLYGON_PATTERN_OPACITY = "polygon-pattern-opacity";
    static final String POLYGON_PATTERN_CLIP = "polygon-pattern-clip";
    static final String POLYGON_PATTERN_SMOOTH = "polygon-pattern-smooth";
    static final String POLYGON_PATTERN_COMP_OP = "polygon-pattern-comp-op";

    // raster
    static final String RASTER_OPACITY = "raster-opacity";
    static final String RASTER_FILTER_FACTOR = "raster-filter-factor";
    static final String RASTER_SCALING = "raster-scaling";
    static final String RASTER_MESH_SIZE = "raster-mesh-size";
    static final String RASTER_COMP_OP = "raster-comp-op";
    
    // point
    static final String POINT_FILE = "point-file";
    static final String POINT_ALLOW_OVERLAP = "point-overlap";
    static final String POINT_IGNORE_PLACEMENT = "point-ignore-placement";
    static final String POINT_OPACITY = "point-opacity";
    static final String POINT_PLACEMENT = "point-placement";
    static final String POINT_TRANSFORM = "point-transform";
    static final String POINT_COMP_OP = "point-comp-op";

    // text
    static final String TEXT_NAME = "text-name";
    static final String TEXT_FACE_NAME = "text-face-name";
    static final String TEXT_SIZE = "text-size";
    static final String TEXT_RATIO = "text-ratio";
    static final String TEXT_WRAP_WIDTH = "text-width";
    static final String TEXT_WRAP_BEFORE = "text-before";
    static final String TEXT_WRAP_CHARACTER = "text-character";
    static final String TEXT_SPACING = "text-spacing";
    static final String TEXT_CHARACTER_SPACING = "text-character-spacing";
    static final String TEXT_LINE_SPACING = "text-line-spacing";
    static final String TEXT_LABEL_POSITION_TOLERANCE = "text-label-position-tolerance";
    static final String TEXT_MAX_CHAR_ANGLE_DELTA = "text-max-char-angle-delta";
    static final String TEXT_FILL = "text-fill";
    static final String TEXT_OPACITY = "text-opacity";
    static final String TEXT_HALO_FILL = "text-halo-fill";
    static final String TEXT_HALO_RADIUS = "text-halo-radius";
    static final String TEXT_DX = "text-dx";
    static final String TEXT_DY = "text-dy";
    static final String TEXT_VERTICAL_ALIGNMENT = "text-vertical-alignment";
    static final String TEXT_AVOID_EDGES = "text-avoid-edges";
    static final String TEXT_MIN_DISTANCE = "text-min-distance";
    static final String TEXT_MIN_PADDING = "text-min-padding";
    static final String TEXT_MIN_PATH_LENGTH = "text-min-path-length";
    static final String TEXT_ALLOW_OVERLAP = "text-allow-overlap";
    static final String TEXT_ORIENTATION = "text-orientiation";
    static final String TEXT_PLACEMENT = "text-placement";
    static final String TEXT_PLACEMENT_TYPE = "text-placement-type";
    static final String TEXT_PLACEMENTS = "text-placements";
    static final String TEXT_TRANSFORM = "text-transform";
    static final String TEXT_HORIZONTAL_ALIGNMENT = "text-horizontal-alignment";
    static final String TEXT_ALIGN = "text-align";
    static final String TEXT_CLIP = "text-clip";
    static final String TEXT_COMP_OP = "text-comp-op";

    // building
    static final String BUILDING_FILL = "building-fill";
    static final String BUILDING_FILL_OPACITY = "building-fill-opacity";
    static final String BUILDING_HEIGHT = "building-height";
}
