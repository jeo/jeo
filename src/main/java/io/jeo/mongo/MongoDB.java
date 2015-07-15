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
package io.jeo.mongo;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.jeo.vector.VectorDriver;
import io.jeo.vector.Schema;
import io.jeo.util.Key;
import io.jeo.util.Messages;
import io.jeo.util.Password;

import static io.jeo.util.Util.set;

public class MongoDB implements VectorDriver<MongoWorkspace> {

    public static final Key<String> DB = new Key<String>("db", String.class);
    
    public static final Key<String> HOST = new Key<String>("host", String.class, "localhost");
    
    public static final Key<Integer> PORT = new Key<Integer>("port", Integer.class, 27017);
    
    public static final Key<String> USER = 
            new Key<String>("user", String.class, System.getProperty("user.name"));
    
    public static final Key<Password> PASSWD = new Key<Password>("passwd", Password.class);

    public static MongoWorkspace open(MongoOpts opts) throws IOException {
        return new MongoWorkspace(opts);
    }

    @Override
    public boolean isEnabled(Messages messages) {
        return true;
    }

    @Override
    public String name() {
        return "MongoDB";
    }

    @Override
    public List<String> aliases() {
        return Arrays.asList("mongo");
    }

    @Override
    public Class<MongoWorkspace> type() {
        return MongoWorkspace.class;
    }

    @Override
    public List<Key<?>> keys() {
        return (List) Arrays.asList(DB, HOST, PORT, USER, PASSWD);
    }

    @Override
    public String family() {
        return "jeo";
    }

    @Override
    public boolean canOpen(Map<?, Object> opts, Messages msgs) {
        if (!DB.in(opts)) {
            Messages.of(msgs).report("No " + DB + " option specified");
            return false;
        }

        return true;
    }

    @Override
    public MongoWorkspace open(Map<?, Object> opts) throws IOException {
        return new MongoWorkspace(MongoOpts.fromMap(opts));
    }

    @Override
    public boolean canCreate(Map<?, Object> opts, Messages msgs) {
        Messages.of(msgs).report("Creation not supported");
        return false;
    }

    @Override
    public MongoWorkspace create(Map<?, Object> opts, Schema schema) throws IOException {
        throw new UnsupportedOperationException();
    }

    static final Set<Capability> CAPABILITIES = set(APPEND, UPDATE, CREATE, DESTROY, BOUND, LIMIT, OFFSET);

    @Override
    public Set<Capability> capabilities() {
        return CAPABILITIES;
    }
}
