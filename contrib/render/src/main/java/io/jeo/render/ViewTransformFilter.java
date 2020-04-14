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
package io.jeo.render;

import io.jeo.map.View;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateFilter;

/**
 * Filter that applies the affine transform of a {@link View}.
 *
 * @author Justin Deoliveira, Boundless
 */
public class ViewTransformFilter implements CoordinateFilter {

    View view;

    public ViewTransformFilter(View view) {
        this.view = view;
    }

    /**
     * Convenience method for chaining, calls {@link #filter(Coordinate)} and returns the 
     * original coordinate.
     */
    public Coordinate apply(Coordinate c) {
        filter(c);
        return c;
    }

    @Override
    public void filter(Coordinate coord) {
        coord.x = coord.x * view.scaleX() + view.translateX();
        coord.y = coord.y * -view.scaleY() + view.translateY();
    }

}
