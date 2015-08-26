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
package io.jeo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.String.format;

/**
 * Build information about the library.
 * 
 * @author Justin Deoliveira, Boundless
 */
public class Jeo {

    /**
     * logger
     */
    public static Logger LOG = LoggerFactory.getLogger(Jeo.class);

    /**
     * Returns the version of this build.
     */
    public static String version() {
        return property("version");
    }

    /**
     * Returns the git revision of this build.
     */
    public static String revision() {
        return property("revision");
    }

    /**
     * Returns the git revision of this build.
     */
    public static String revisionShort() {
        String rev = property("revision");
        return rev != null ? rev.substring(0, 7) : null;
    }

    /**
     * Returns the timestamp of this build.
     */
    public static Date buildDate() {
        String buildDate = property("buildDate"); 
        try {
            return buildDate != null ? dateFormatISO().parse(buildDate) : null;
        } catch (ParseException e) {
            LOG.debug("Error parsing build date: " + buildDate, e);
        }
        return null;
    }

    /**
     * Returns the jeo home directory.
     * <p>
     * The jeo home directory can be set with the <tt>jeo.home</tt> system property. If unset it
     * falls back to a directory named <tt>.jeo</tt> under the users home directory. If that directory
     * can not be created a temporary directory is created.
     * </p>
     */
    public static Path home() throws IOException {
        // first check jeo.home
        String h = System.getProperty("jeo.home");
        if (h != null) {
            Path p =  Paths.get(h);
            File f = p.toFile();
            if (!f.exists()) {
                throw new IOException(format(Locale.ROOT, "jeo.home was set to %s but does not exist", h));
            }

            if (!f.canRead()) {
                throw new IOException(format(Locale.ROOT, "jeo.home was set to %s but is not readable", h));
            }

            return p;
        }

        // next try under %HOME%/.jeo
        if (h == null) {
            h = System.getProperty("user.home");
            if (h != null) {
                Path p = Paths.get(h).resolve(".jeo");
                File f = p.toFile();
                if (f.exists()) {
                    return p;
                }

                if (f.mkdirs()) {
                    return p;
                }

                LOG.debug("Unable to create jeo home directory: {}", f.getPath());
            }
        }

        return Files.createTempDirectory("jeo");
    }

    static SimpleDateFormat dateFormatISO() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ROOT);
    }

    static SimpleDateFormat dateFormatHuman() {
        return new SimpleDateFormat("MMM dd yyyy", Locale.ROOT);
    }
    
    static String property(String key) {
        try {
            InputStream in = Jeo.class.getResourceAsStream("build.properties");
            try {
                Properties props = new Properties();
                props.load(in);
                return props.getProperty(key);
            }
            finally {
                in.close();
            }
        }
        catch(IOException e) {
            LOG.debug("Error getting build property " + key, e);
        }
        return null;
    }

    /**
     * Prints version info to stdout.
     */
    public static void main(String[] args) {
        printVersionInfo(System.out);
    }

    /**
     * Prints version info for the library.
     */
    public static void printVersionInfo(PrintStream out) {
        out.println(format(Locale.ROOT, "jeo %s (%s, %s)", version(), revisionShort(),
            dateFormatHuman().format(buildDate())));
    }
}
