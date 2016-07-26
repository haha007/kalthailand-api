package th.co.krungthaiaxa.api.elife.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author khoi.tran on 7/26/16.
 */
public class IOUtil {
    private static final Logger LOG = LoggerFactory.getLogger(IOUtil.class);

    /**
     * @param pathString relative resource, such as "/email-content/email-phone-wrong-number.txt".
     * @return
     */
    public static String loadTextFile(String pathString) {
        Path path = Paths.get(pathString);
        try {
            byte[] encoded = Files.readAllBytes(path);
            return new String(encoded, "UTF-8");
        } catch (IOException e) {
            LOG.error("Cannot load String from " + path, e);
            return null;
        }
    }
}
