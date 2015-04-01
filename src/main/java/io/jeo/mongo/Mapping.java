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
package io.jeo.mongo;

import java.util.ArrayList;
import java.util.List;

/**
 * Declares mappings of a mongo object/document to a feature.
 * <p>
 * This object supports two types of mapping. The first is mapping of to a geometry object 
 * with the {@link #geometry(String)} method. For example consider the following mongo document
 * representing a country: 
 * 
 * <pre>
 * {
 *   "geometry": {
 *      "type": "Polygon", 
 *      "coordinates": [...]
 *    }, 
 *    "name": "Canada", 
 *    "capital": 
 *      "name": "Ottawa",
 *      "loc": {
 *         "type": "Point", 
 *         "coordinates": [...]
 *      }
 *   }
 * </pre>
 * 
 * To map the geometry of the country itself, along with the geometry of the captital city:
 * <pre><code>
 * new Mapping().geometry("geometry").geometry("captial.loc");
 * </code></pre>
 * 
 * The first mapped geometry is considered to be the primary geometry.
 * </p>
 * <p>
 * The second type of mapping is providing a root path to an object whose properties should be 
 * considered properties of the feature with the {@link #properties(String)} method. For example, 
 * consider the following country object:
 * 
 * <pre>
 * {
 *   "geometry": {
 *      "type": "Polygon", 
 *      "coordinates": [...]
 *    }, 
 *    "info": {
 *      "name": "Canada",
 *      "population": 35056064 
 *    }
 * </pre>
 * 
 * To map the object:
 * <pre><code>
 * new Mapping().geometry("geometry").properties("info");
 * </code></pre>
 * </p>
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class Mapping {

    List<Path> geo = new ArrayList<Path>();
    Path prop = new Path();

    /**
     * Specifies a geometry object mapping.
     * 
     * @param path Path to the geometry object, nested paths specified with the '.' separator.  
     */
    public Mapping geometry(String path) {
        geo.add(new Path(path));
        return this;
    }

    /**
     * Specifies a mapping to the property object.
     * 
     * @param path Path to the property object, nested paths specified with the '.' separator.  
     */
    public Mapping properties(String path) {
        prop = new Path(path);
        return this;
    }

    Path getGeometryPath(String key) {
        Path p = null;
        for (Path g : geo) {
            if (g.join().equals(key)) {
                p = g;
                break;
            }
        }
        return p;
    }

    List<Path> getGeometryPaths() {
        return geo;
    }

    Path geometry() {
        if (geo.isEmpty()) {
            throw new IllegalStateException("No geometry paths");
        }

        return geo.get(0);
    }

    Path getPropertyPath() {
        return prop;
    }

}
