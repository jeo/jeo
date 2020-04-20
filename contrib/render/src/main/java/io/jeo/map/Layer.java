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
package io.jeo.map;

import io.jeo.data.Dataset;
import io.jeo.filter.Filter;

/**
 * A layer of a map.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class Layer {

    String name;
    String title;
    Dataset data;
    Filter filter;

    boolean visible = true;

    public Layer() {
    }

    public Layer(Dataset data) {
        this.data = data;
        this.name = data.name();
    }

    /**
     * Name of the layer.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the layer.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Human readable title of the layer.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the human readable title of the layer.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Dataaset of the layer.
     */
    public Dataset getData() {
        return data;
    }

    /**
     * Sets the dataaset of the layer.
     */
    public void setData(Dataset data) {
        this.data = data;
    }

    /**
     * Filter for data access.
     */
    public Filter getFilter() {
        return filter;
    }

    /**
     * Sets filter for data access.
     */
    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    /**
     * Visibility flag for layer.
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Sets visibility flag for layer.
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
