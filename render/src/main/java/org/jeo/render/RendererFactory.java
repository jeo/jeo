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

import java.util.List;
import java.util.Map;

import org.jeo.map.View;

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
     * Name of renderer.
     */
    String getName();

    /**
     * List of secondary aliases the the renderer is identified by.
     */
    List<String> getAliases();

    /**
     * List of image formats the render can produce.
     * <p>
     * This list contains "well known names" for the format including common file
     * name extension, and mime types. For example if the renderer supports the
     * PNG format this list would include "png", "image/png", etc...
     * </p>
     */
    List<String> getFormats();

    /**
     * Creates a renderer instance.
     *
     * @param view The view to be rendered.
     * @param opts Rendering specific options.
     * 
     * @return A new instance of the renderer.
     */
    T create(View view, Map<?,Object> opts);
}
