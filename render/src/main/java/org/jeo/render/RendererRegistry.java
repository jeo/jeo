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

import java.util.Iterator;

/**
 * Interface for registry for renderers. 
 * <p>
 * Typically the registry is not used directly to work with renderer implementations, the 
 * {@link Renderers} class is used for that purpose.
 * </p>
 * @author Justin Deoliveira, Boundless
 */
public interface RendererRegistry {

    /**
     * Lists all the renderers in this registry.
     */
    Iterator<RendererFactory<?>> list();
}
