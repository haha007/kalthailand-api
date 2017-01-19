package th.co.krungthaiaxa.api.common.test.utils;

import org.junit.Assert;
import org.junit.Test;
import th.co.krungthaiaxa.api.common.utils.MimeTypeUtil;

/**
 * @author khoi.tran on 12/4/16.
 */
public class MimeTypeUtilsTest {
    @Test
    public void testSuccessGetFileExtension() {
        testExtensionFromMimeType("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        testExtensionFromMimeType("xls", "application/vnd.ms-excel");
        testExtensionFromMimeType("pdf", "application/pdf");

    }

    private void testExtensionFromMimeType(String extension, String mimeType) {
        Assert.assertEquals(extension, MimeTypeUtil.getFileExtensionFromMimeType(mimeType));
    }

    @Test
    public void testSuccessGetFileMimetype() {
        Assert.assertEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", MimeTypeUtil.getMimeTypeFromFileExtension("xlsx"));
        Assert.assertEquals("application/vnd.ms-excel", MimeTypeUtil.getMimeTypeFromFileExtension("xls"));
        Assert.assertEquals("application/pdf", MimeTypeUtil.getMimeTypeFromFileExtension("pdf"));
    }
}
