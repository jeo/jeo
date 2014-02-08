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
package org.jeo.map.render;

import java.util.List;
import java.util.Map;

import org.jeo.util.Key;

/**
 * Factory for {@link Renderer} instances.
 * <p>
 * Implementations of this class should be thread safe and generally maintain no state. 
 * </p>
 * @author Justin Deoliveira, Boundless
 *
 */
public interface RendererFactory<T extends Renderer> {

    /**
     * Key to specify width of surface to render to.
     */
    static Key<Integer> WIDTH = new Key<Integer>("width", Integer.class, 256);

    /**
     * Key to specify height of surface to render to.
     */
    static Key<Integer> HEIGHT = new Key<Integer>("height", Integer.class, 256);

    /**
     * Name of renderer.
     */
    String getName();

    /**
     * List of secondary aliases the the renderer is identified by.
     */
    List<String> getAliases();

    /**
     * Creates a renderer instance.
     * 
     * @param opts Rendering specific options.
     * 
     * @return A new instance of the renderer.
     */
    T create(Map<?,Object> opts);
}
