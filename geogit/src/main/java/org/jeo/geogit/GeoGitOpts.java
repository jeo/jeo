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

import static org.jeo.geogit.GeoGit.*;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jeo.util.Key;

public class GeoGitOpts {

    File file;
    
    String user = USER.def();
    String email = EMAIL.def();
    boolean emailIsSet = false;

    public static GeoGitOpts fromMap(Map<?,Object> map) {
        GeoGitOpts ggopts = new GeoGitOpts(FILE.get(map));

        if (USER.in(map)) {
            ggopts.user(USER.get(map));
        }
        if (EMAIL.in(map)) {
            ggopts.email(EMAIL.get(map));
        }

        return ggopts;
    }

    public GeoGitOpts(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }
    
    public GeoGitOpts user(String user) {
        this.user = user;
        if (!emailIsSet) {
            email = user + "@localhost";
        }
        return this;
    }

    public String getUser() {
        return user;
    }

    public GeoGitOpts email(String email) {
        this.email = email;
        this.emailIsSet = true;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public Map<Key<?>,Object> toMap() {
        LinkedHashMap<Key<?>, Object> map = new LinkedHashMap<Key<?>, Object>();
        map.put(FILE, file);
        map.put(USER, user);
        map.put(EMAIL, email);

        return map;
    }
}
