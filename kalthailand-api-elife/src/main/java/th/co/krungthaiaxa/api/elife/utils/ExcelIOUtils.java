package th.co.krungthaiaxa.api.elife.utils;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import th.co.krungthaiaxa.api.common.exeption.FileNotFoundException;
import th.co.krungthaiaxa.api.common.utils.IOUtil;

import java.io.IOException;
import java.io.InputStream;

public class ExcelIOUtils {
    public static Workbook loadFileFromClassPath(String classPath) {
        InputStream inputStream = IOUtil.loadInputStreamFileInClassPath(classPath);
        try {
            return new XSSFWorkbook(inputStream);
        } catch (IOException e) {
            String msg = "Cannot load file '" + classPath + "': " + e.getMessage();
            throw new FileNotFoundException(msg, e);
        }
        //TODO Not sure should we close it or not.
        /*finally {
            IOUtil.closeIfPossible(inputStream);
        }*/
    }

    public static byte[] writeToBytes(Workbook workbook) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            workbook.write(byteArrayOutputStream);
            byte[] bytes = byteArrayOutputStream.toByteArray();
            return bytes;
        } catch (IOException e) {
            throw new FileNotFoundException("Cannot write workbook to bytes: " + e.getMessage(), e);
        }
    }
}
