package org.jeo.map;

import org.jeo.data.Dataset;

/**
 * A layer of a map.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class Layer {

    String name;
    String title;
    Dataset data;

    boolean visible = true;

    public Layer() {
    }

    public Layer(Dataset data) {
        this.data = data;
        this.name = data.getName();
        this.title = data.getTitle() != null ? data.getTitle() : name;
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
