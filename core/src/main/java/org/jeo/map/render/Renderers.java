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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jeo.map.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Renderer utility class.
 *
 * @author Justin Deoliveira, Boundless
 */
public class Renderers {

    /** logger */
    static final Logger LOG = LoggerFactory.getLogger(Renderers.class);

    /** renderer registry */
    public static final RendererRegistry REGISTRY = new ServiceLoaderRendererRegistry();

    /**
     * Lists all available renderer factories from the default registry.
     */
    public static Iterator<RendererFactory<?>> list() {
        return list(REGISTRY);
    }

    /**
     * Lists all available renderer factories from the specified registry.
     */
    public static Iterator<RendererFactory<?>> list(RendererRegistry reg) {
        return reg.list();
    }

    /**
     * Looks up a renderer factory by name.
     *
     * @see #factory(String, RendererRegistry)
     */
    public static RendererFactory<?> factory(String name) {
        return factory(name, REGISTRY);
    }

    /**
     * Looks up a renderer factory by name from the specified registry.
     *
     * @return The renderer factory or <code>null</code> if no such factory is found for the specified name.
     */
    public static RendererFactory<?> factory(String name, RendererRegistry reg) {
        for (Iterator<RendererFactory<?>> it = reg.list(); it.hasNext();) {
            RendererFactory<?> rf = it.next();
            if (name.equalsIgnoreCase(rf.getName())) {
                return rf;
            }
            for (String alias : rf.getAliases()) {
                if (name.equalsIgnoreCase(alias)) {
                    return rf;
                }
            }
        }
        return null;
    }

    /**
     * Lists all renderer factories that can produce the specified format.
     *
     * @param format The name of the format.
     *
     * @see RendererFactory#getFormats()
     */
    public static Iterator<RendererFactory<?>> listForFormat(String format) {
        return listForFormat(format, REGISTRY);
    }

    /**
     * Lists all renderer factories in the specified registry that can produce the specified format.
     *
     * @param format The name of the format.
     * @param reg The registry of renderers.
     *
     * @see RendererFactory#getFormats()
     */
    public static Iterator<RendererFactory<?>> listForFormat(String format, RendererRegistry reg) {
        reg = reg != null ? reg : REGISTRY;
        List<RendererFactory<?>> factories = new ArrayList<RendererFactory<?>>();
        for (Iterator<RendererFactory<?>> it = reg.list(); it.hasNext(); ) {
            RendererFactory<?> rf = it.next();
            for (String supportedFormat : rf.getFormats()) {
                if (supportedFormat.equalsIgnoreCase(format)) {
                    factories.add(rf);
                }
            }
        }
        return factories.iterator();
    }

    /**
     * Creates a new renderer from name, view, and options.
     *
     * @see {@link #factory(String, RendererRegistry)}
     */
    public static Renderer create(String name, View view, Map<?,Object> opts) {
        return create(name, view, opts, REGISTRY);
    }

    /**
     * Creates a new renderer from name, view, options and the specified registry.
     *
     * @throws java.lang.IllegalArgumentException If no such render matching <tt>name</tt> exists.
     */
    public static Renderer create(String name, View view, Map<?,Object> opts, RendererRegistry reg) {
        RendererFactory<?> rf = factory(name, reg);
        if (rf == null) {
            throw new IllegalArgumentException("no renderer named " + name);
        }

        return rf.create(view, opts);
    }
}
