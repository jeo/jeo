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

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Loads renderers with {@link ServiceLoader}.
 * <p>
 * This registry picks up renderer factorties registered via the file META-INF/services/org.jeo.render.RendererFactory
 * </p>
 *
 * @author Justin Deoliveira, OpenGeo
 */
public class ServiceLoaderRendererRegistry implements RendererRegistry {

    @Override
    public Iterator<RendererFactory<?>> list() {
        return (Iterator) ServiceLoader.load(RendererFactory.class).iterator();
    }

}
