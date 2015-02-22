/* Copyright 2015 The jeo project. All rights reserved.
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
package org.jeo.protobuf;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.jeo.data.FileDriver;
import org.jeo.util.Messages;
import org.jeo.vector.VectorDriver;
import org.jeo.vector.Schema;

/**
 * Google Protocol Buffer (Protobuf) driver.
 * <p>
 * Usage:
 * <pre><code>
 * Protobuf.open(new File("states.pbf"));
 * </code></pre>
 * </p>
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class Protobuf extends FileDriver<ProtobufDataset> implements VectorDriver<ProtobufDataset> {

    public static ProtobufDataset open(File file) throws IOException {
        return new ProtobufDataset(file);
    }

    @Override
    public String name() {
        return "Protobuf";
    }

    @Override
    public List<String> aliases() {
        return Arrays.asList("pb", "pbf");
    }

    @Override
    public Class<ProtobufDataset> type() {
        return ProtobufDataset.class;
    }
    
    @Override
    public ProtobufDataset open(File file, Map opts) throws IOException {
        return open(file);
    }

    @Override
    public boolean supports(Capability cap) {
        return false;
    }

    @Override
    public boolean canCreate(Map<?, Object> opts, Messages msgs) {
        return FILE.in(opts);
    }

    @Override
    public ProtobufDataset create(Map<?, Object> opts, Schema schema) throws IOException {
        return new ProtobufDataset(FILE.get(opts), schema);
    }
}
