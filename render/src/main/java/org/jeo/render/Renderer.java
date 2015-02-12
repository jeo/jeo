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

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import org.jeo.data.Disposable;
import org.jeo.map.View;

/**
 * Map renderer.
 * <p>
 * Instances of this interface are created through the corresponding {@link RendererFactory} 
 * interface whose responsibility is to create instances of this interface. Implementations of this
 * interface need not be thread safe.
 * </p>
 * 
 * @see {@link RendererFactory}
 */
public interface Renderer extends Disposable {

    /**
     * Initializes the renderer for a new job.
     * 
     * @param view The view to render.
     * @param opts Rendering specific options.
     */
    void init(View view, Map<?,Object> opts);

    /**
     * Starts the render job.
     *
     * @param output An output stream to render to.
     */
    void render(OutputStream output) throws IOException;
}
