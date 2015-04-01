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
import java.io.IOException;

import io.jeo.Tests;
import org.junit.Rule;
import org.junit.rules.TestName;

public class GeoPkgTestSupport {

    //uncomment to view debug logs during test
//    @org.junit.BeforeClass
//    public static void logging() {
//      
//      java.util.logging.Logger log = java.util.logging.Logger.getLogger(
//          org.slf4j.LoggerFactory.getLogger(GeoPackage.class).getName());
//      log.setLevel(java.util.logging.Level.FINE);
//    
//      java.util.logging.ConsoleHandler h = new java.util.logging.ConsoleHandler();
//      h.setLevel(java.util.logging.Level.FINE);
//      log.addHandler(h);
//    }

    @Rule public TestName name = new TestName();

    protected File newTmpDir() throws IOException {
        return Tests.newTmpDir("geopkg", name.getMethodName());
    }
}
