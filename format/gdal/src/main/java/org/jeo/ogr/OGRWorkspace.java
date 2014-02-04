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
package org.jeo.ogr;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gdal.ogr.DataSource;
import org.gdal.ogr.Layer;
import org.jeo.data.Dataset;
import org.jeo.data.Driver;
import org.jeo.data.FileData;
import org.jeo.data.Handle;
import org.jeo.data.VectorDataset;
import org.jeo.data.Workspace;
import org.jeo.feature.Schema;
import org.jeo.util.Key;

public class OGRWorkspace implements Workspace, FileData {

    File file;
    OGR driver;

    public OGRWorkspace(File file, OGR driver) {
        this.file = file;
        this.driver = driver;
    }

    @Override
    public Driver<?> getDriver() {
        return driver;
    }

    @Override
    public Map<Key<?>, Object> getDriverOptions() {
        LinkedHashMap<Key<?>, Object> opts = new LinkedHashMap<Key<?>, Object>();
        opts.put(OGR.FILE, file);
        opts.put(OGR.DRIVER, driver.getName());
        return opts;
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public Iterable<Handle<Dataset>> list() throws IOException {
        DataSource data = open();
        try {
            List<Handle<Dataset>> list = new ArrayList<Handle<Dataset>>();
            for (int i = 0; i < data.GetLayerCount(); i++) {
                Layer l = data.GetLayer(i);
                list.add(Handle.to(l.GetName(), this));
                l.delete();
            }
            return list;
        }
        finally {
            data.delete();
        }
    }

    public OGRDataset get(int index) throws IOException {
        DataSource data = open();
        try {
            Layer l = data.GetLayer(index);
            try {
                if (l != null) {
                    return new OGRDataset(l.GetName(), this);
                }
            }
            finally {
                if (l != null) l.delete();
            }
        }
        finally {
            data.delete();
        }
        return null;
    }

    @Override
    public OGRDataset get(String layer) throws IOException {
        DataSource data = open();
        try {
            Layer l = data.GetLayerByName(layer);
            try {
                if (l != null) {
                    return new OGRDataset(layer, this);
                }
            }
            finally {
                if (l != null) l.delete();
            }
        }
        finally {
            data.delete();
        }
        return null;
    }

    @Override
    public VectorDataset create(Schema schema) throws IOException {
        throw new UnsupportedOperationException();
    }

    DataSource open() throws IOException {
        DataSource data = driver.ogrDrv.Open(file.getAbsolutePath());
        if (data == null) {
            throw new IOException("Unable to open " + file);
        }
        return data;
    }

    @Override
    public void close() {
    }
}
