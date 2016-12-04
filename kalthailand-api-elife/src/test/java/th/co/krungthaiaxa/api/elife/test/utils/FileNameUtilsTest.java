package th.co.krungthaiaxa.api.elife.test.utils;

import org.junit.Assert;
import org.junit.Test;
import th.co.krungthaiaxa.api.common.utils.FileNameUtil;

/**
 * @author khoi.tran on 12/4/16.
 */
public class FileNameUtilsTest {
    @Test
    public void success_get_file_extension() {
        Assert.assertEquals("xlsx", FileNameUtil.getFileExtensionFromMimeType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        Assert.assertEquals("xls", FileNameUtil.getFileExtensionFromMimeType("application/vnd.ms-excel"));
        Assert.assertEquals("pdf", FileNameUtil.getFileExtensionFromMimeType("application/pdf"));

    }

    @Test
    public void success_get_file_mimetype() {
        Assert.assertEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", FileNameUtil.getMimeTypeFromFileExtension("xlsx"));
        Assert.assertEquals("application/vnd.ms-excel", FileNameUtil.getMimeTypeFromFileExtension("xls"));
        Assert.assertEquals("application/pdf", FileNameUtil.getMimeTypeFromFileExtension("pdf"));

    }
}
