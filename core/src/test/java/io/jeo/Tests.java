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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Test utility class.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class Tests {

    /**
     * The root temp directory.
     * <p>
     * Will resolve to "target" under the current working directory if it exists (i.e is
     *  being run as part of a maven build). Otherwise it will resolve to "java.io.tmpdir".
     * </p>
     */
    public static Path tmpRoot() {
        Path target = Paths.get("target");
        File file = target.toFile();
        if (file.exists() && file.isDirectory()) {
            return target;
        }

        return Paths.get(System.getProperty("java.io.tmpdir"));
    }

    /**
     * Creates a new temp directory under the {@link #tmpRoot()}  directory of the calling module.
     * 
     * @return The handle to the directory.
     */
    public static Path newTmpDir() throws IOException {
        return newTmpDir("jeo", "test");
    }

    /**
     * Creates a new temp file under the "target" directory of the calling module.
     * 
     * @return The handle to the file.
     */
    public static Path newTmpFile() throws IOException {
        return newTmpFile(null);
    }

    /**
     * Creates a new temp file under the "target" directory of the calling module with the 
     * specified contents.
     * 
     * @return The handle to the file.
     */
    public static Path newTmpFile(InputStream contents) throws IOException {
        return newTmpFile("jeo", "test", contents);
    }

    /**
     * Creates a new temp directory under the "target" directory of the calling module.
     * <p>
     * The <tt>prefix</tt> parameters are those as passed to
     * {@link Files#createTempDirectory(String, FileAttribute[])}
     * </p>
     *
     * @return The handle to the directory.
     */
    public static Path newTmpDir(String prefix, String suffix) throws IOException {
        Path dir = Files.createTempDirectory(tmpRoot(), prefix);
        dir.toFile().mkdirs();
        return dir;
    }

    /**
     * Creates a new temp file under the "target" directory of the calling module with the specified
     * prefix/suffix and contents.
     * 
     * @return The handle to the file.
     */
    public static Path newTmpFile(String prefix, String suffix, final InputStream contents)
        throws IOException {
        Path file = Files.createTempFile(tmpRoot(), prefix, suffix);
        if (contents != null) {
            Files.copy(contents, file, StandardCopyOption.REPLACE_EXISTING);
        }
        return file;
    }

    /**
     * Unzips a zipped input stream into the specified directory.
     * 
     * @return The originaly specified directory.
     */
    public static Path unzip(final InputStream archive, final Path dir) throws IOException {
    
        ZipInputStream zipin = new ZipInputStream(archive);
        ZipEntry zipEntry = null;
    
        while((zipEntry = zipin.getNextEntry()) != null) {
            if (zipEntry.isDirectory())
                continue;

            Path targetFile = dir.resolve(zipEntry.getName());
            targetFile.getParent().toFile().mkdirs();

            Files.copy(zipin, targetFile);
        }
    
        zipin.close();
        return dir;
    }

    /**
     * Unzips a gzipped resource.
     * 
     * @param gzipped The gzipped content.
     * @param dir The target directory to gunzip to.
     * @param filename The filename to gunzip to.
     * 
     */
    public static Path gunzip(final InputStream gzipped, final Path dir, String filename)
        throws IOException {
        GZIPInputStream gzipin = new GZIPInputStream(gzipped);

        Path file = dir.resolve(filename);
        Files.copy(gzipin, file);

        return file;
    }

    /**
     * Configures JDK logging to log at debug (FINE) level.
     *
     * @param id The id of the logger to configure.
     */
    public static void debugLogging(String id) {
        Logger root = Logger.getLogger(id);
        root.setLevel(Level.FINE);

        ConsoleHandler console = null;
        for (Handler h : root.getHandlers()) {
            if (h instanceof ConsoleHandler) {
                console = (ConsoleHandler) h;
                break;
            }
        }

        if (console == null) {
            console = new ConsoleHandler();
            root.addHandler(console);
        }

        console.setLevel(Level.FINE);
    }
}
