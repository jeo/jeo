package org.jeo.map;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.jeo.data.Disposable;
import org.jeo.proj.Proj;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Compose data and styles for rendering.
 * <p>
 * A map maintains a collection of {@link Layer} objects containing the data composing the map. The
 * order of the layer objects in {@link #getLayers()} defines the rendering/drawing/z order for 
 * renderers. The {@link #getStyle()} contains the symbolization rules for the layers of the map.
 * </p>
 * <p>
 * A map has dimensions ({@link #getWidth()} and {@link #getHeight()}) in addition to a world 
 * extent specified by {@link #getBounds()}. THe bounds together with {@link #get    
 * </p>
 *  
 * @author Justin Deoliveira, OpenGeo
 */
public class Map implements Disposable {

    /**
     * Listener callback.
     */
    public static interface Listener {

        /**
         * Callback fired when the bounds of the map are changed.
         *  
         * @param map The map.
         * @param bounds The new bounds of the map.
         * @param old The old bounds of the map, possibly <tt>null</tt>.
         */
        void onBoundsChanged(Map map, Envelope bounds, Envelope old);

        /**
         * Callback fired when the projection of the map are changed.
         *  
         * @param map The map.
         * @param crs The new projection of the map.
         * @param old The old projection of the map, possibly <tt>null</tt>.
         */
        void onCRSChanged(Map map, CoordinateReferenceSystem crs, CoordinateReferenceSystem old);

        /**
         * Callback fired when the dimensions of the map are changed.
         *  
         * @param map The map.
         * @param width The new width of the map.
         * @param height The new height of the map.
         * @param oldWidth The old width of the map, possibly <tt>-1</tt>.
         * @param oldHeight The old height of the map, possibly <tt>-1</tt>.
         */
        void onSizeChanged(Map map, int width, int height, int oldWidth, int oldHeight);

        /**
         * Callback fired when the style of the map is changed.
         *  
         * @param map The map.
         * @param style The new style of the map.
         * @param old The old style of the map, possibly <tt>null</tt>.
         */
        void onStyleChanged(Map map, Style style, Style old);
    }

    /**
     * default map width. 
     */
    public static int DEFAULT_WIDTH = 256;

    /**
     * default map height.
     */
    public static int DEFAULT_HEIGHT = 256;

    /**
     * Returns a new map builder.
     */
    public static MapBuilder build() {
        return new MapBuilder();
    }

    static Logger LOG = LoggerFactory.getLogger(Map.class);

    int width = DEFAULT_WIDTH;
    int height = DEFAULT_HEIGHT;

    Envelope bounds = new Envelope(-180,180,-90,90);
    CoordinateReferenceSystem crs = Proj.EPSG_4326;

    List<Layer> layers = new ArrayList<Layer>();

    Style style = new Style();

    List<Disposable> cleanup = new ArrayList<Disposable>(); 
    Set<Listener> callbacks = new LinkedHashSet<Listener>();

    /**
     * Creates a new empty map.
     */
    public Map() {
    }

    /**
     * Creates a new map initializing properties from an existing map.
     */
    public Map(Map other) {
        width = other.getWidth();
        height = other.getHeight();
        bounds = other.getBounds();
        crs = other.getCRS();
        layers = new ArrayList<Layer>(other.getLayers());
        style = other.getStyle();
        cleanup = new ArrayList<Disposable>(other.getCleanup());
    }

    /**
     * The width of the map in "rendering" units, usually pixels. 
     */
    public int getWidth() {
        return width;
    }

    /**
     * Sets the width of the map in "rendering" units, usually pixels. 
     */
    public void setWidth(int width) {
        int oldWidth = this.width;
        this.width = width;
        fireSizeChanged(oldWidth, height);
    }

    /**
     * The height of the map in "rendering" units, usually pixels. 
     */
    public int getHeight() {
        return height;
    }

    /**
     * Sets the height of the map in "rendering" units, usually pixels. 
     */
    public void setHeight(int height) {
        int oldHeight = this.height;
        this.height = height;
        fireSizeChanged(width, oldHeight);
    }

    public void setSize(int width, int height) {
        int oldWidth = this.width;
        int oldHeight = this.height;

        this.width = width;
        this.height = height;

        fireSizeChanged(oldWidth, oldHeight);
    }

    /**
     * The spatial/world extent of the map.
     */
    public Envelope getBounds() {
        return bounds;
    }

    /**
     * Sets the spatial/world extent of the map.
     */
    public void setBounds(Envelope bounds) {
        Envelope oldBounds = this.bounds;
        this.bounds = bounds;
        fireBoundsChanged(oldBounds);
    }

    /**
     * The projection / coordinate reference system of the map.
     */
    public CoordinateReferenceSystem getCRS() {
        return crs;
    }

    /**
     * Sets the projection / coordinate reference system of the map.
     */
    public void setCRS(CoordinateReferenceSystem crs) {
        CoordinateReferenceSystem oldCRS = this.crs;
        this.crs = crs;
        fireCRSChanged(oldCRS);
    }

    /**
     * The layers composing the map.
     */
    public List<Layer> getLayers() {
        return layers;
    }

    /**
     * The style containing the rules for rendering/symbolizing the map.  
     */
    public Style getStyle() {
        return style;
    }

    /**
     * Sets the stylesheet containing the rules for rendering/symbolizing the map.  
     */
    public void setStyle(Style style) {
        Style oldStyle = this.style;
        this.style = style;
        fireStyleChanged(oldStyle);
    }

    /**
     * The horizontal scaling factor of the affine transform that maps points in world space to 
     * points in rendering space, defined as <pre>map.width / bounds.width</pre>.
     */
    public double scaleX() {
        return getWidth() / bounds.getWidth();
    }

    /**
     * The horizontal scaling factor of the affine transform that maps points rendering space to  
     * points in world space, defined as <pre>bounds.width / map.width</pre>.
     */
    public double iscaleX() {
        return bounds.getWidth() / (double) getWidth();
    }

    /**
     * The vertical scaling factor of the affine transform that maps points in world space to 
     * points in rendering space, defined as <pre>map.height / bounds.height</pre>.
     */
    public double scaleY() {
        return getHeight() / bounds.getHeight();
    }

    /**
     * The vertical scaling factor of the affine transform that maps points rendering space to  
     * points in world space, defined as <pre>bounds.height / map.height</pre>.
     */
    public double iscaleY() {
        return bounds.getHeight() / (double) getHeight();
    }

    /**
     * The horizontal translation factor of the affine transform that maps points in world space to 
     * points in rendering space, defined as <pre>-(bounds.minx * xscale)</pre>.
     */
    public double translateX() {
        return -bounds.getMinX() * scaleX();
    }

    /**
     * The vertical translation factor of the affine transform that maps points in world space to 
     * points in rendering space, defined as <pre>bounds.miny * yscale + map.height</pre>.
     */
    public double translateY() {
        return (bounds.getMinY() * scaleY()) + getHeight();
    }

    /**
     * Disposable resources to dispose when the map is disposed. 
     */
    List<Disposable> getCleanup() {
        return cleanup;
    }

    /**
     * Binds a callback to the map for listening to map property changed. 
     * <p>
     * The application should eventually call {@link #unbind(Listener)} although the 
     * callback list is cleared when {@link #close()} is called. 
     * </p>
     */
    public void bind(Listener callback) {
        callbacks.add(callback);
    }

    /**
     * Unbinds a previously bound callback from the map.
     */
    public void unbind(Listener callback) {
        callbacks.remove(callback);
    }

    void fireBoundsChanged(Envelope old) {
        for (Listener cb : callbacks) {
            try {
                cb.onBoundsChanged(this, bounds, old);
            }
            catch(Throwable t) {
                LOG.debug("Callback failed", t.getMessage(), t);
            }
        }
    }

    void fireStyleChanged(Style old) {
        for (Listener cb : callbacks) {
            try {
                cb.onStyleChanged(this, style, old);
            }
            catch(Throwable t) {
                LOG.debug("Callback failed", t.getMessage(), t);
            }
        }
    }

    void fireSizeChanged(int oldWidth, int oldHeight) {
        for (Listener cb : callbacks) {
            try {
                cb.onSizeChanged(this, width, height, oldWidth, oldHeight);
            }
            catch(Throwable t) {
                LOG.debug("Callback failed", t.getMessage(), t);
            }
        }
    }

    void fireCRSChanged(CoordinateReferenceSystem old) {
        for (Listener cb : callbacks) {
            try {
                cb.onCRSChanged(this, crs, old);
            }
            catch(Throwable t) {
                LOG.debug("Callback failed", t.getMessage(), t);
            }
        }
    }

    @Override
    public void close() {
        for (Disposable d : cleanup) {
            d.close();
        }

        layers.clear();
        callbacks.clear();
    }

    
}
