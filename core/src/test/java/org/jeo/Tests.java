package org.jeo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;

/**
 * Test utility class.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class Tests {

    /**
     * Creates a new temp directory under the "target" directory of the calling module.
     * 
     * @return The handle to the directory.
     */
    public static File newTmpDir() throws IOException {
        return newTmpDir("jeo", "test");
    }

    /**
     * Creates a new temp file under the "target" directory of the calling module.
     * 
     * @return The handle to the file.
     */
    public static File newTmpFile() throws IOException {
        return newTmpFile(null);
    }

    /**
     * Creates a new temp file under the "target" directory of the calling module with the 
     * specified contents.
     * 
     * @return The handle to the file.
     */
    public static File newTmpFile(InputStream contents) throws IOException {
        return newTmpFile("jeo", "test", contents);
    }

    /**
     * Creates a new temp directory under the "target" directory of the calling module.
     * <p>
     * The <tt>prefix</tt> and <tt>suffix</tt> parameters are those as passed to 
     * {@link File#createTempFile(String, String)}
     * </p>
     *
     * @return The handle to the directory.
     */
    public static File newTmpDir(String prefix, String suffix) throws IOException {
        File dir = File.createTempFile(prefix, suffix, new File("target"));
        dir.delete();
        dir.mkdirs();
        return dir;
    }

    /**
     * Creates a new temp file under the "target" directory of the calling module with the specified
     * prefix/suffix and contents.
     * 
     * @return The handle to the file.
     */
    public static File newTmpFile(String prefix, String suffix, final InputStream contents) 
        throws IOException {
        File file = File.createTempFile(prefix, suffix, new File("target"));
        if (contents != null) {
            Files.copy(new InputSupplier<InputStream>() {
                @Override
                public InputStream getInput() throws IOException {
                    return contents;
                }
            }, file);
        }
        return file;
    }

    /**
     * Unzips a zipped input stream into the specified directory.
     * 
     * @return The originaly specified directory.
     */
    public static File unzip(final InputStream archive, final File dir) 
        throws ZipException, IOException {
    
        ZipInputStream zipin = new ZipInputStream(archive);
        ZipEntry zipEntry = null;
    
        while((zipEntry = zipin.getNextEntry()) != null) {
            if (zipEntry.isDirectory())
                continue;
    
            final File targetFile = new File(dir, zipEntry.getName());
            Files.createParentDirs(targetFile);
    
            ByteStreams.copy(zipin, Files.newOutputStreamSupplier(targetFile).getOutput());
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
    public static File gunzip(final InputStream gzipped, final File dir, String filename) 
        throws IOException {
        GZIPInputStream gzipin = new GZIPInputStream(gzipped);

        File file = new File(dir, filename);
        ByteStreams.copy(gzipin, Files.newOutputStreamSupplier(file).getOutput());

        return file;
    }
}
