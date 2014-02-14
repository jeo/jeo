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
package org.jeo.geojson.parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.jeo.util.Optional;

public class Node {

    String name;
    Object value;

    Node parent;
    LinkedList<Node> children = new LinkedList<Node>();

    Node(String name, Node parent) {
        this.name = name;
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public Node getParent() {
        return parent;
    }

    public List<Node> getChildren() {
        return children;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public <T> T value(Class<T> clazz) {
        return clazz.cast(value);
    }

    public Node newNode(String name) {
        Node n = new Node(name, this);
        children.add(n);
        return n;
    }

    public Node find(String name) {
        for (Node n : children) {
            if (n.getName().equals(name)) {
                return n;
            }
        }
        return null;
    }

    public <T> Optional<T> consume(String name, Class<T> clazz) {
        for (Iterator<Node> it = children.iterator(); it.hasNext(); ) {
            Node n = it.next();
            if (n.getName().equals(name)) {
                it.remove();
                return Optional.of(clazz.cast(n.getValue()));
            }
        }

        return Optional.of((T)null);
    }

    public <T> List<T> consumeAll(String name, Class<T> clazz) {
        List<T> all = new ArrayList<T>();
        for (Iterator<Node> it = children.iterator(); it.hasNext(); ) {
            Node n = it.next();
            if (n.getName().equals(name)) {
                it.remove();
                all.add((T) n.getValue());
            }
        }

        return all;
    }

    public Node lastChild() {
        return children.isEmpty() ? null : children.getLast();
    }

    @Override
    public String toString() {
        return new StringBuilder().append(name).append(" = ").append(value).toString();
    }
}
