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

import java.io.IOException;
import java.io.StringWriter;
import org.jeo.data.DataRepositoryView;
import org.jeo.data.Dataset;
import org.jeo.data.Workspace;
import org.jeo.util.XMLWriter;

/**
 *
 */
public abstract class OWSHandler extends Handler {

    protected final String serviceName;

    OWSHandler(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public final boolean canHandle(Request request, NanoServer server) {
        String uri = request.getUri().toLowerCase();
        String match = "/" + serviceName;
        return uri.equals(match) || uri.equals(match + "/");
    }

    @Override
    public final NanoHTTPD.Response handle(Request req, NanoServer server) throws Exception {
        String service = req.parms.getProperty("service", serviceName);
        if (!serviceName.equalsIgnoreCase(serviceName)) {
            throw exception("InvalidParameterValue", "service", "no service named: " + service);
        }
        String request = req.parms.getProperty("request");
        if (request == null) {
            throw exception("MissingParameterValue", "request", "request parameter missing");
        }

        DelegateHandler handler = resolveRequestHandler(request.toLowerCase(), req);
        if (handler == null) {
            throw exception("OperationNotSupported", "request", "No operation : " + request);
        }
        return handler.handle(req, server);
    }

    Dataset resolve(DataRepositoryView registry, String wsDatasetSpec, RequestParser parser) throws IOException {
        Dataset resolved = null;
        String[] parts = wsDatasetSpec.split(":", 2);
        if (parts.length == 1) {
            parts = new String[]{parts[0], parts[0]};
        }

        Workspace ws = registry.get(parts[0], Workspace.class);
        if (ws == null) {
            parser.addError("No workspace: " + parts[0]);
        } else {
            resolved = ws.get(parts[1]);
            if (resolved == null) {
                parser.addError("No layer " + parts[1] + " in workspace " + parts[0]);
            }
        }
        return resolved;
    }

    protected abstract DelegateHandler resolveRequestHandler(String request, Request req);

    
    static HttpException exception(String code, String locator, String message) {
        StringWriter writer = new StringWriter();
        XMLWriter xml = new XMLWriter(writer);
        xml.start("ServiceExceptionReport");
        xml.element("ServiceException", message, "code", code, "locator", locator);
        xml.end("ServiceExceptionReport");
        try {
            xml.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        throw new HttpException(NanoHTTPD.HTTP_OK, writer.toString(), "text/xml");
    }
}
