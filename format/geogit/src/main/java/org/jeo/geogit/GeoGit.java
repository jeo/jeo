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
package org.jeo.geogit;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.geogit.api.GeoGIT;
import org.geogit.api.porcelain.ConfigOp;
import org.geogit.api.porcelain.ConfigOp.ConfigAction;
import org.geogit.di.GeogitModule;
import org.geogit.repository.Repository;
import org.geogit.storage.bdbje.JEStorageModule;
import org.jeo.data.FileVectorDriver;
import org.jeo.data.VectorDriver;
import org.jeo.feature.Schema;
import org.jeo.util.Key;
import org.jeo.util.Messages;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;

public class GeoGit extends FileVectorDriver<GeoGitWorkspace> {

    /**
     * Username key, defaults to <tt>System.getProperty("user.name")</tt>
     */
    public static final Key<String> USER = 
        new Key<String>("user.name", String.class, System.getProperty("user.name"));

    /**
     * Email key, defaults to <tt>System.getProperty("user.name") + "@localhost"</tt>
     */
    public static final Key<String> EMAIL = 
        new Key<String>("user.email", String.class, USER.getDefault() + "@localhost");

    public static GeoGitWorkspace open(GeoGitOpts opts) throws IOException {
        return new GeoGitWorkspace(newGeoGIT(opts), opts);
    }

    @Override
    public String getName() {
        return "GeoGIT";
    }

    @Override
    public List<Key<?>> getKeys() {
        return (List) Arrays.asList(FILE, /*CREATE, */USER, EMAIL);
    }

    @Override
    public Class<GeoGitWorkspace> getType() {
        return GeoGitWorkspace.class;
    }

    @Override
    protected boolean canOpen(File file, Map<?,Object> opts, Messages msgs) {
        if (!super.canOpen(file, opts, msgs)) {
            return false;
        }

        if (!file.isDirectory()) {
            Messages.of(msgs).report(file.getPath() + " is not a directory");
            return false;
        }

        File dotgg = new File(file, ".geogit");
        if (!dotgg.exists()) {
            Messages.of(msgs).report(file.getPath() + " is not a geogit repository");
            return false;
        }

        return true;
    }

    @Override
    public GeoGitWorkspace open(File file, Map<?, Object> opts) throws IOException {
        GeoGitOpts ggopts = ggopts(file, opts);
        return new GeoGitWorkspace(newGeoGIT(ggopts), ggopts);
    }

    @Override
    protected GeoGitWorkspace create(File file, Map<?, Object> opts, Schema schema) 
        throws IOException {

        GeoGitWorkspace ws = open(file, opts);
        ws.create(schema);
        return ws;
    }

    GeoGitOpts ggopts(File file, Map<?,Object> opts) {
        GeoGitOpts ggopts = new GeoGitOpts(file);

        if (USER.has(opts)) {
            ggopts.user(USER.get(opts));
        }
        if (EMAIL.has(opts)) {
            ggopts.email(EMAIL.get(opts));
        }

        return ggopts;
    }

    static GeoGIT newGeoGIT(GeoGitOpts opts) throws IOException {
        File file = opts.getFile();

        //TODO: something about this
        Injector i = Guice.createInjector(
            Modules.override(new GeogitModule()).with(new JEStorageModule()));

        GeoGIT gg = new GeoGIT(i, file);
        gg.getOrCreateRepository();

        Repository repo = gg.getRepository();
        repo.command(ConfigOp.class).setAction(ConfigAction.CONFIG_SET)
            .setName("user.name").setValue(opts.getUser()).call();
        repo.command(ConfigOp.class).setAction(ConfigAction.CONFIG_SET)
            .setName("user.email").setValue(opts.getEmail()).call();
        
        return gg;
    }

    static final EnumSet<Capability> CAPABILITIES = EnumSet.of(Capability.BOUND);

    @Override
    public boolean supports(VectorDriver.Capability cap) {
        return CAPABILITIES.contains(cap);
    }
}
