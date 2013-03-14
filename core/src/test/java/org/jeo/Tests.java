package org.jeo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;

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

}
