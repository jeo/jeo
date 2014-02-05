package org.jeo.map.render;

import org.jeo.feature.Feature;
import org.jeo.map.Rule;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class AnchoredLabel extends Label {

    /**
     * Anchor point of the label
     */
    Coordinate anchor;

    public AnchoredLabel(String text, Rule rule, Feature feature, Geometry geom, Coordinate anchor) {
        super(text, rule, feature, geom);
        this.anchor = anchor;
    }

    public Coordinate getAnchor() {
        return anchor;
    }
}
