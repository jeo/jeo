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
package io.jeo.geopkg;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import io.jeo.data.FileDriver;
import io.jeo.util.Key;
import io.jeo.util.Password;

public class GeoPkgOpts {

    File file;
    String user;
    Password passwd;

    public static GeoPkgOpts fromMap(Map<?,Object> map) {
        return new GeoPkgOpts(FileDriver.FILE.get(map)).user(GeoPkgBaseDriver.USER.get(map)).passwd(GeoPkgBaseDriver.PASSWD.get(map));
    }

    public GeoPkgOpts(File file) {
        this.file = file;
    }

    public GeoPkgOpts user(String user) {
        this.user = user;
        return this;
    }

    public GeoPkgOpts passwd(Password passwd) {
        this.passwd = passwd;
        return this;
    }

    public File getFile() {
        return file;
    }

    public String getUser() {
        return user;
    }

    public Password getPasswd() {
        return passwd;
    }

    public Map<Key<?>,Object> toMap() {
        Map<Key<?>,Object> map = new LinkedHashMap<Key<?>, Object>();
        map.put(FileDriver.FILE, file);
        if (user != null) {
            map.put(GeoPkgBaseDriver.USER, user);
        }
        if (passwd != null) {
            map.put(GeoPkgBaseDriver.PASSWD, passwd);
        }
        return map;
    }
}
