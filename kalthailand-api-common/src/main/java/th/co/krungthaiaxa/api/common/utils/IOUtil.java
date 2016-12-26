package th.co.krungthaiaxa.api.common.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import th.co.krungthaiaxa.api.common.exeption.FileIOException;
import th.co.krungthaiaxa.api.common.exeption.FileNotFoundException;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * @author khoi.tran on 7/26/16.
 */
public class IOUtil {
    public static final Logger LOGGER = LoggerFactory.getLogger(IOUtil.class);

    public static void closeIfPossible(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException e) {
            //Only write log and then the flow is still processed as normal. Don't interrupt the process.
            LOGGER.error("Cannot close object: " + e.getMessage(), e);
        }
    }

    /**
     * @param path a relative path in classpath. E.g. "images/email/logo.png"
     *             From Class, the path is relative to the package of the class unless you include a leading slash.
     *             So if you don't want to use the current package, include a slash like this: "/SomeTextFile.txt"
     * @return
     */
    public static String loadTextFileInClassPath(String path) {
        try {
            InputStream inputStream = loadInputStreamFromClassPath(path);
            return IOUtils.toString(inputStream, Charset.forName("UTF-8"));
        } catch (IOException e) {
            String msg = String.format("Cannot load String from file '%s'", path);
            throw new FileNotFoundException(msg, e);
        }
    }

    public static File createParentFolderIfNecessary(String filePath) {
        File destinationFile = new File(filePath);
        String parentPath = destinationFile.getParent();
        return createFolderIfNecessary(parentPath);
    }

    public static File createFolderIfNecessary(String filePath) {
        File folder = new File(filePath);
        try {
            if (!folder.exists()) {
                FileUtils.forceMkdir(folder);
            }
        } catch (IOException e) {
            throw new FileIOException(String.format("Cannot create folder '%s'", folder.getAbsolutePath()), e);
        }
        return folder;
    }

    public static File createFile(String filePath) {
        File destinationFile = new File(filePath);
        createParentFolderIfNecessary(filePath);
        if (!destinationFile.exists()) {
            try {
                destinationFile.createNewFile();
            } catch (IOException e) {
                throw new FileIOException(String.format("Cannot create file '%s': %s", destinationFile.getAbsolutePath(), e.getMessage()), e);
            }
        }
        return destinationFile;
    }

    public static InputStream getInputStream(File file) {
        try {
            return new FileInputStream(file);
        } catch (java.io.FileNotFoundException e) {
            throw new FileIOException(String.format("Cannot get input stream from file '%s': %s", file.getAbsoluteFile(), e.getMessage()), e);
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
            return IOUtils.toByteArray(loadInputStreamFromClassPath(path));
        } catch (IOException e) {
            String msg = String.format("Cannot load String from '%s'", path);
            throw new FileNotFoundException(msg, e);
        }
    }

    /**
     * This method never return null.
     *
     * @param path
     * @return
     */
    public static InputStream loadInputStreamFromClassPath(String path) {
        InputStream result = IOUtil.class.getResourceAsStream(path);
        if (result == null) {
            throw new FileIOException("Cannot load file from classpath: " + path);
        }
        return result;
    }

    public static File writeBytesToRelativeFile(String relativeFilePath, byte[] bytes) {
        File file = new File(relativeFilePath);
        try {
            FileUtils.writeByteArrayToFile(file, bytes);
            LOGGER.debug("Write bytes to file: " + file.getAbsolutePath());
            return file;
        } catch (IOException e) {
            throw new FileIOException("Cannot write bytes to file " + file.getAbsolutePath() + ": " + e.getMessage(), e);
        }
    }

    public static void writeInputStream(File file, InputStream inputStream) {
        try {
            FileUtils.writeByteArrayToFile(file, IOUtils.toByteArray(inputStream));
        } catch (IOException e) {
            throw new FileIOException(String.format("Cannot write data to file '%s'", file.getAbsolutePath()), e);
        }

    }
}
