package org.jeo;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Build information about the library.
 * 
 * @author Justin Deoliveira, Boundless
 */
public class JEO {

    /**
     * logger
     */
    public static Logger LOG = LoggerFactory.getLogger(JEO.class);

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

    static SimpleDateFormat dateFormatISO() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    }

    static SimpleDateFormat dateFormatHuman() {
        return new SimpleDateFormat("MMM dd yyyy");
    }
    
    static String property(String key) {
        try {
            InputStream in = JEO.class.getResourceAsStream("build.properties");
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
        out.println(String.format("jeo %s (%s, %s)", version(), revisionShort(), 
            dateFormatHuman().format(buildDate())));
    }
}
