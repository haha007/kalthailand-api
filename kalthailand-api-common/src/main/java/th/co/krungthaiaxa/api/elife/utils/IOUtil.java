package th.co.krungthaiaxa.api.elife.utils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import th.co.krungthaiaxa.api.elife.exeption.FileNotFoundException;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.apache.commons.io.IOUtils.toByteArray;

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
        try {
            return IOUtils.toString(IOUtil.class.getResourceAsStream(pathString), Charset.forName("UTF-8"));
        } catch (IOException e) {
            String msg = String.format("Cannot load String from '%s'", pathString);
            throw new FileNotFoundException(msg, e);
        }
    }

    /**
     * @param path a relative path. E.g. "/images/email/logo.png"
     * @return
     */
    public static byte[] loadBinaryFile(String path) {
        try {
            return IOUtils.toByteArray(IOUtil.class.getResourceAsStream(path));
        } catch (IOException e) {
            String msg = String.format("Cannot load String from '%s'", path);
            throw new FileNotFoundException(msg, e);
        }
    }
}
