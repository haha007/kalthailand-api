package th.co.krungthaiaxa.api.common.utils;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import th.co.krungthaiaxa.api.common.exeption.FileNotFoundException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * @author khoi.tran on 7/26/16.
 */
public class IOUtil {
    public static final Logger LOG = LoggerFactory.getLogger(IOUtil.class);

    /**
     * @param path a relative path in classpath. E.g. "images/email/logo.png"
     *             From Class, the path is relative to the package of the class unless you include a leading slash.
     *             So if you don't want to use the current package, include a slash like this: "/SomeTextFile.txt"
     * @return
     */
    public static String loadTextFileInClassPath(String path) {
        try {
            return IOUtils.toString(IOUtil.class.getResourceAsStream(path), Charset.forName("UTF-8"));
        } catch (IOException e) {
            String msg = String.format("Cannot load String from '%s'", path);
            throw new FileNotFoundException(msg, e);
        }
    }

    /**
     * @param path a relative path in classpath. E.g. "images/email/logo.png"
     *             From Class, the path is relative to the package of the class unless you include a leading slash.
     *             So if you don't want to use the current package, include a slash like this: "/SomeTextFile.txt"
     * @return
     */
    public static byte[] loadBinaryFileInClassPath(String path) {
        try {
            return IOUtils.toByteArray(IOUtil.class.getResourceAsStream(path));
        } catch (IOException e) {
            String msg = String.format("Cannot load String from '%s'", path);
            throw new FileNotFoundException(msg, e);
        }
    }

    public static InputStream loadInputStreamFileInClassPath(String path) {
        return IOUtil.class.getResourceAsStream(path);
    }
}
