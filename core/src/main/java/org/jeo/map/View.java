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

import java.util.LinkedHashSet;
import java.util.Set;

import org.jeo.proj.Proj;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Encapsulates the bounding box and screen/view size of a {@link Map}.
 *  
 * @author Justin Deoliveira, OpenGeo
 */
public class View {

    /**
     * Listener interface that receives callbacks on changes to the viewport.
     */
    public static interface Listener {
        /**
         * Callback fired when the bounds of the view are changed.
         *  
         * @param view The view.
         * @param bounds The new bounds of the view.
         * @param old The old bounds of the view, possibly <tt>null</tt>.
         */
        void onBoundsChanged(View view, Envelope bounds, Envelope old);
    
        /**
         * Callback fired when the projection of the view are changed.
         *  
         * @param view The view.
         * @param crs The new projection of the view.
         * @param old The old projection of the view, possibly <tt>null</tt>.
         */
        void onCRSChanged(View view, CoordinateReferenceSystem crs, CoordinateReferenceSystem old);
    
        /**
         * Callback fired when the dimensions of the view are changed.
         *  
         * @param view The view.
         * @param width The new width of the view.
         * @param height The new height of the view.
         * @param oldWidth The old width of the view, possibly <tt>-1</tt>.
         * @param oldHeight The old height of the view, possibly <tt>-1</tt>.
         */
        void onSizeChanged(View view, int width, int height, int oldWidth, int oldHeight);
    }

    /**
     * default view width. 
     */
    public static int DEFAULT_WIDTH = 256;

    /**
     * default view height.
     */
    public static int DEFAULT_HEIGHT = 256;

    static Logger LOG = LoggerFactory.getLogger(Map.class);

    Map map;

    Envelope bounds = new Envelope(-180,180,-90,90);
    CoordinateReferenceSystem crs = Proj.EPSG_4326;
    int width = DEFAULT_WIDTH;
    int height = DEFAULT_HEIGHT;

    Set<Listener> callbacks = new LinkedHashSet<Listener>();

    View(Map map) {
        this.map = map;
    }

    public Map getMap() {
        return map;
    }
    
    /**
     * The width of the map in "rendering" units, usually pixels. 
     */
    public int getWidth() {
        return width;
    }

    /**
     * Sets the width of the map in "rendering" units, usually pixels.
     * <p>
     * Application code should generally only call this method during initialization of the 
     * map/view and instead call {@link #resize(int, int)} to change the dimensions of the view
     * dynamically. This method will not result in any events being fired. 
     * </p>
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * The height of the map in "rendering" units, usually pixels. 
     */
    public int getHeight() {
        return height;
    }

    /**
     * Sets the height of the map in "rendering" units, usually pixels.
     * <p>
     * Application code should generally only call this method during initialization of the 
     * map/view and instead call {@link #resize(int, int)} to change the dimensions of the view
     * dynamically. This method will not result in any events being fired. 
     * </p> 
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * Changes the dimensions of the view.
     * <p>
     * This method results in {@link Listener#onSizeChanged(View, int, int, int, int)} 
     * being fired.
     * </p>
     * @param newWidth The new width of the view.
     * @param newHeight The new height of the view.
     */
    public void resize(int newWidth, int newHeight) {
        int oldWidth = this.width;
        int oldHeight = this.height;
        setWidth(newWidth);
        setHeight(newHeight);
        fireSizeChanged(oldWidth, oldHeight);
    }

    /**
     * The spatial/world extent of the view.
     */
    public Envelope getBounds() {
        return bounds;
    }

    /**
     * Sets the spatial/world extent of the view.
     * <p>
     * Application code should generally only call this method during initialization of the 
     * map/view and instead call {@link #zoomto(Envelope))} to change the bounds of the view
     * dynamically. This method will not result in any events being fired. 
     * </p>
     */
    public void setBounds(Envelope bounds) {
        this.bounds = bounds;
    }

    /**
     * Changes the spatial/world extent of the view.
     * <p>
     * This method results in {@link Listener#onBoundsChanged(View, Envelope, Envelope)} 
     * being fired.
     * </p>
     * @param newBounds The new extent.
     */
    public void zoomto(Envelope newBounds) {
       Envelope oldBounds = bounds;
       setBounds(newBounds);
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
     * <p>
     * Application code should generally only call this method during initialization of the 
     * map/view and instead call {@link #reproject(CoordinateReferenceSystem)))} to change the 
     * projection of the view dynamically. This method will not result in any events being fired.
     * </p>
     */
    public void setCRS(CoordinateReferenceSystem crs) {
        this.crs = crs;
    }

    /**
     * Changes the projection of the view.
     * <p>
     * This method results in 
     * {@link Listener#onCRSChanged(View, CoordinateReferenceSystem, CoordinateReferenceSystem)} 
     * being fired.
     * </p>
     * @param newCRS The new projection of the view.
     */
    public void reproject(CoordinateReferenceSystem newCRS) {
        CoordinateReferenceSystem oldCRS = crs;
        setCRS(newCRS);
        fireCRSChanged(oldCRS);
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
     * Binds a callback to the viewport for listening to view property changes. 
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

    @Override
    public View clone() {
        View v = new View(map);
        v.setBounds(bounds);
        v.setCRS(crs);
        v.setWidth(width);
        v.setHeight(height);
        v.callbacks.addAll(callbacks);
        return v;
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

}
