package th.co.krungthaiaxa.api.elife.test.utils;

import org.junit.Test;
import th.co.krungthaiaxa.api.elife.utils.PdfTestUtil;

/**
 * @author khoi.tran on 10/26/16.
 */

public class PdfTemplatePositionsTest {
    @Test
    public void generatePdfTemplatePositions() {
        PdfTestUtil.writePositionsOnPdfTemplate("/ereceipt/EreceiptTemplate_with_note.pdf");
    }
}
