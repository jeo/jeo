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
package org.jeo.nano;

import java.io.File;

import org.jeo.nano.NanoHTTPD.Response;

public class AppsHandler extends Handler {

    File appsDir = null;

    public AppsHandler() {
        this(null);
    }

    public AppsHandler(File appsDir) {
        this.appsDir = appsDir;
    }

    @Override
    public void init(NanoServer server) {
        if (appsDir == null) {
            if (server.getWWWRoot() == null) {
                throw new IllegalStateException(
                    "www root not set, unable to create relative apps directory");
            }

            appsDir = new File(server.getWWWRoot(), "apps");
            if (!appsDir.exists()) {
                if (!appsDir.mkdirs()) {
                    throw new IllegalStateException(
                        "unable to create apps directory: " + appsDir.getPath());
                }
            }
        }
    }

    @Override
    public boolean canHandle(Request request, NanoServer server) {
        String uri = request.getUri(); 
        return uri.startsWith("/apps") || uri.startsWith("/ol");
    }

    @Override
    public Response handle(Request request, NanoServer server) throws Exception {
        return NanoServer.serveFile(request.getUri(), request.getHeader(), appsDir, true, "/apps");
    }

}
