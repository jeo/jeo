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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.jeo.data.Disposable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
         * Callback fired when the style of the map is changed.
         *  
         * @param map The map.
         * @param style The new style of the map.
         * @param old The old style of the map, possibly <tt>null</tt>.
         */
        void onStyleChanged(Map map, Style style, Style old);
    }

    /**
     * Returns a new map builder.
     */
    public static MapBuilder build() {
        return new MapBuilder();
    }

    static Logger LOG = LoggerFactory.getLogger(Map.class);

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

    @Override
    public void close() {
        for (Disposable d : cleanup) {
            d.close();
        }

        layers.clear();
        callbacks.clear();
    }
}
