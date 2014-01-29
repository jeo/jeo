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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.junit.Rule;
import org.junit.rules.TestName;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jeo.nano.NanoHTTPD.Response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class HandlerTestSupport {

    @Rule
    public TestName name = new TestName();

    Handler handler;
    MockServer mock;

    Properties q(String... s) {
        Properties p = new Properties();
        for (int i = 0; i < s.length; i+=2) {
            p.setProperty(s[i], s[i+1]);
        }
        return p;
    }
    
    Properties h(String... s) {
        return q(s);
    }
    
    Properties body(String data) throws IOException {
        File tmp = File.createTempFile("data", name.getMethodName(), new File("target"));
    
        
        ByteStreams.copy(new ByteArrayInputStream(data.getBytes()), 
            Files.newOutputStreamSupplier(tmp));
    
        Properties p = new Properties();
        p.setProperty("content", tmp.getAbsolutePath());
        return p;
    }

    String read(Response resp) throws IOException {
        return new String(ByteStreams.toByteArray(resp.stream()));
    }

    String dequote(String json) {
        return json.replaceAll("'", "\"");
    }

    String quote(String json) {
        return json.replaceAll("\"", "\'");
    }

    void testWorkspaceDataPattern(Pattern pattern, String prefix, boolean testEmpty, boolean fileSuffix) {
        List<String[]> args = new ArrayList();
        if (testEmpty) {
            args.add(new String[]{ null, null });
        }
        args.addAll(Arrays.asList(new String[][] {
            { "singledata", null },
            { "workspace", "dataset" },
            { "work_space", "data_set" },
            { "work-space", "data-set" },
            { "Work-space", "Data-set" },
        }));
        if (fileSuffix) {
            for (int i = 0, ii = args.size(); i < ii; i++) {
                args.add(new String[] { args.get(i)[0], args.get(i)[1], "ext" });
            }
        }
        for (int i = 0; i < args.size(); i++) {
            String[] caseArgs = args.get(i);
            String uri = prefix;
            if (caseArgs[0] != null) {
                uri += "/" + caseArgs[0];
            }
            if (caseArgs[1] != null) {
                uri += "/" + caseArgs[1];
            }
            if (caseArgs.length > 2) {
                uri += "." + caseArgs[2];
            }
            assertPattern(pattern, uri, caseArgs);
        }
    }

    Response makeRequest(Request request, String expectedStatus, String expectedMime) throws Exception {
        assertTrue(handler.canHandle(request, mock.server));
        Response resp = handler.handle(request, mock.server);
        assertEquals(expectedStatus, resp.status);
        assertEquals(expectedMime, resp.mimeType);
        return resp;
    }

    void makeBadRequest(Request request, String expectedStatus, String expectedMessage) throws Exception {
        assertTrue(handler.canHandle(request, mock.server));
        try {
            handler.handle(request, mock.server);
            fail("expected HttpException");
        } catch (HttpException he) {
            assertEquals(expectedStatus, he.status);
            assertEquals(expectedMessage, he.content);
        }
    }

    void assertPattern(Pattern pattern, String test, String... groups) {
        Matcher matcher = pattern.matcher(test);
        assertTrue("expected match for " + test, matcher.matches());
        for (int i = 0; i < groups.length; i++) {
            if (groups[i] != null) {
                int group = i + 1;
                assertEquals("For group " + group + " on " + test, groups[i], matcher.group(group));
            }
        }
    }

    void assertContains(String text, String substring) {
        assertTrue("expected to find substring: " + substring, text.contains(substring));
    }

    void assertNoTemplatePlaceholders(String text) {
        String[] patterns = new String[] {
            "%\\w+%"
        };
        for (String s: patterns) {
            Pattern pat = Pattern.compile(s);
            if (pat.matcher(text).find()) {
                fail("expected to not find anything matching " + s);
            }
        }
    }

    /**
     * Test our assertion to verify it does find bad stuff
     */
    @Test
    public void testAssertNoTemplatePlaceholders() {
        try {
            assertNoTemplatePlaceholders("${x}");
            assertNoTemplatePlaceholders("%foo%");
            fail();
        } catch (AssertionError ae) {
            // yay
        }
    }

    /**
     * Check if the singleQuotedJSON matches the response.
     * If no singleQuotedJSON is provided, fail and single quote the response -
     * this is useful for creating the expected response string
     * @param response
     * @param singleQuotedJSON
     * @throws IOException
     */
    void assertJSONEquals(Response response, String singleQuotedJSON) throws IOException {
        assertEquals(NanoHTTPD.HTTP_OK, response.status);
        assertEquals(NanoHTTPD.MIME_JSON, response.mimeType);
        if (singleQuotedJSON == null) {
            System.out.println("RESPONSE WAS:");
            System.out.println(quote(read(response)));
            fail("singleQuotedJSON not provided - response printed in stdout");
        }
        assertEquals(dequote(singleQuotedJSON), read(response));
    }

}
