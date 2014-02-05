/* Copyright 2014 The jeo project. All rights reserved.
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
package org.jeo.svg;

/**
 * Text font.
 */
class Font {

    static final float DEFAULT_SIZE = 14f;

    enum Weight {
        normal, bold, bolder, lighter, inherit
    }

    enum Style {
        normal, italic, oblique, inherit
    }

    enum Variant {
        normal, smallcaps, inherit
    }

    String family = "sans-serif";
    Weight weight = Weight.normal;
    Style style = Style.normal;
    float size = DEFAULT_SIZE;
    Unit unit = Unit.pixel;
}
