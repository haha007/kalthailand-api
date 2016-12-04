package th.co.krungthaiaxa.api.common.test.utils;

import org.junit.Assert;
import org.junit.Test;
import th.co.krungthaiaxa.api.common.utils.FileNameUtil;

/**
 * @author khoi.tran on 12/4/16.
 */
public class FileNameUtilsTest {
    @Test
    public void testSuccessGetFileExtension() {
        testExtensionFromMimeType("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        testExtensionFromMimeType("xls", "application/vnd.ms-excel");
        testExtensionFromMimeType("pdf", "application/pdf");

    }

    private void testExtensionFromMimeType(String extension, String mimeType) {
        Assert.assertEquals(extension, FileNameUtil.getFileExtensionFromMimeType(mimeType));
    }

    @Test
    public void testSuccessGetFileMimetype() {
        Assert.assertEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", FileNameUtil.getMimeTypeFromFileExtension("xlsx"));
        Assert.assertEquals("application/vnd.ms-excel", FileNameUtil.getMimeTypeFromFileExtension("xls"));
        Assert.assertEquals("application/pdf", FileNameUtil.getMimeTypeFromFileExtension("pdf"));
    }
}
