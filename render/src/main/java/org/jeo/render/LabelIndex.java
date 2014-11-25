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
package org.jeo.render;

import static org.jeo.map.CartoCSS.*;

import java.util.ArrayList;
import java.util.List;

import org.jeo.data.VectorDataset;
import org.jeo.data.mem.MemVector;
import org.jeo.feature.BasicFeature;
import org.jeo.feature.Feature;
import org.jeo.feature.Schema;

import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.index.quadtree.Quadtree;

/**
 * Stores {@link Label} objects in a spatial index dealing with label overlapping.
 *
 * @author Justin Deoliveira, OpenGeo
 *
 * TODO: use prepared geometries for overlap computation
 */
public class LabelIndex {

    Quadtree idx;

    public LabelIndex() {
        idx = new Quadtree();
    }

    /**
     * Queries the index for labels that overlap the specified label.
     */
    public List<Label> query(Label label) {
        List<Label> labels = new ArrayList<Label>();
        for (Label close : (List<Label>)idx.query(label.bounds())) {
            if (close.shape().intersects(label.shape())) {
                labels.add(close);
            }
        }

        return labels;
    }

    /**
     * Inserts a label into the index taking into account overlapping labels.
     * <p>
     * When a label overlap occurs the conflict is resolved by first comparing the user defined 
     * priority of the label, obtained from {@link Label#priority()}. If the priority of the 
     * incoming label is less (or no priority specified) than labels currently in the index, the 
     * incoming label is discarded and not added to the index. If the incoming label is deemed
     * higher priority then any conflicting labels are removed from the underlying index.
     * </p>
     *
     * @return True if the label was added to the index, otherwise false.
     */
    public boolean insert(Label label) {
        boolean add = true;

        if (!allowOverlap(label)) {
            for (Label overlap : query(label)) {
                // conflict, examine priority
                if (label.priority().compareTo(overlap.priority()) > 0) {
                    // kick out existing label
                    idx.remove(overlap.bounds(), overlap);
                }
                else {
                    // existing label one, ignore this one
                    add = false;
                    break;
                }
            }
        }

        if (add) {
            idx.insert(label.bounds(), label);
        }

        return add;
    }

    /**
     * Returns all the labels in the index.
     */
    public Iterable<Label> all() {
        return idx.queryAll();
    }

    /**
     * Returns the labels in the index as a vector dataset.
     * <p>
     * The resulting feature geometry is {@link Label#shape()} and has a "text" attribute coming 
     * from {@link Label#getText()}.
     * </p>
     */
    public VectorDataset features() {
        MemVector mem = new MemVector(Schema.build("labels")
            .field("geometry", Polygon.class).field("text", String.class).schema());
        for (Label l : all()) {
            Feature f = new BasicFeature(null, mem.schema());
            f.put(l.shape());
            f.put("text", l.getText());
            mem.add(f);
        }

        return mem;
    }

    boolean allowOverlap(Label label) {
        return label.getRule().bool(label.getFeature(), TEXT_ALLOW_OVERLAP, false);
    }
}
