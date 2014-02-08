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

/**
 * Interface for label rendering.
 *
 */
public interface Labeller {

    /**
     * No-op labeller.
     */
    static Labeller NULL = new Labeller() {
        @Override
        public void render(Label label) {
        }
        @Override
        public boolean layout(Label label, LabelIndex labels) {
            return false;
        }
    };

    /**
     * Lays out a label for rendering.
     * <p>
     * Given a label the job of this method is to determine where that label will eventually be rendered, if at all.
     * This method may choose to not render the label in which case it should return <tt>false</tt>.
     * </p>
     * @param label The label to layout.
     * @param labels The label index.
     *
     * @return <tt>true</tt> if the label is to be rendered, otherwise <tt>false</tt>.
     */
    boolean layout(Label label, LabelIndex labels);

    /**
     * Renders a label.
     * <p>
     * Only labels that result in {@link #layout(Label, LabelIndex)} returning true are passed to this method.
     * </p>
     * @param label The label to render.
     */
    void render(Label label);
}
