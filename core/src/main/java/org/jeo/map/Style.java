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
package org.jeo.map;

import java.util.Collection;

/**
 * Defines the rules used to symbolize a map.
 * <p>
 * </p> 
 * @author Justin Deoliveira, OpenGeo
 */
public class Style {

    /**
     * Returns a new style builder.
     */
    public static StyleBuilder build() {
        return new StyleBuilder();
    }

    /**
     * Create a new combined style from the provided styles.
     *
     * @param styles non-null collection of styles
     * @return a new Style representing the combination of the provided styles
     */
    public static Style combine(Collection<Style> styles) {
        Style combined = new Style();
        for (Style s: styles) {
            combined.rules.addAll(s.rules);
        }
        return combined;
    }

    RuleList rules = new RuleList();

    /**
     * The rules making up the style.
     */
    public RuleList getRules() {
        return rules;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Rule r : rules) {
            sb.append(r).append("\n");
        }
        return sb.toString();
    }
}
