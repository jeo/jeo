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
package org.jeo.gdal;

import org.gdal.gdal.Dataset;
import org.gdal.gdal.Driver;
import org.gdal.gdal.gdal;
import org.jeo.data.FileDriver;
import org.jeo.data.RasterDriver;
import org.jeo.util.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Justin Deoliveira, Boundless
 */
public class GDAL extends FileDriver<GDALDataset> implements RasterDriver<GDALDataset> {

    static final Logger LOG = LoggerFactory.getLogger(GDAL.class);

    public static void init() throws Throwable {
        if (gdal.GetDriverCount() == 0) {
            gdal.AllRegister();
        }
    }

    static {
        try {
            init();
        }
        catch(Throwable e) {
            LOG.debug("gdal initialization failed", e);
        }
    }

    public static GDALDataset open(File file) throws IOException {
        return new GDAL().open(file, null);
    }

    Driver gdalDrv;

    public GDAL() {
    }

    protected GDAL(Driver gdalDrv) {
        this.gdalDrv = gdalDrv;
    }

    @Override
    public String getName() {
        return gdalDrv != null ? gdalDrv.getShortName() : "GDAL";
    }

    @Override
    public Class<GDALDataset> getType() {
        return GDALDataset.class;
    }

    @Override
    public boolean canOpen(Map<?, Object> opts, Messages msgs) {
        try {
            init();
        }
        catch(Throwable t) {
            Messages.of(msgs).report(t);
            return false;
        }

        return super.canOpen(opts, msgs);
    }

    @Override
    protected boolean canOpen(File file, Map<?, Object> opts, Messages msgs) {
        Driver drv = gdalDrv != null ? gdalDrv : gdal.IdentifyDriver(file.getAbsolutePath());
        if (drv == null) {
            String msg = "Unable to locate driver";
            String lastErrMsg = gdal.GetLastErrorMsg();
            if (lastErrMsg != null) {
                msg += ": " + lastErrMsg;
            }

            Messages.of(msgs).report(msg);
            return false;
        }
        return super.canOpen(file, opts, msgs);
    }

    @Override
    protected GDALDataset open(File file, Map<?, Object> opts) throws IOException {
        Dataset ds = gdal.OpenShared(file.getAbsolutePath());
        if (ds == null) {
            String lastErrMsg = gdal.GetLastErrorMsg();
            String msg = "Unable to open file: " + file;
            if (lastErrMsg != null) {
                msg += ", " + lastErrMsg;
            }
            throw new IOException(msg);
        }

        return new GDALDataset(file, ds, this);
    }
}