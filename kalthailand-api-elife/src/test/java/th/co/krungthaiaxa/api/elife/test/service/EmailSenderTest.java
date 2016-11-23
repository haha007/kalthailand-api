package th.co.krungthaiaxa.api.elife.test.service;

import th.co.krungthaiaxa.api.common.utils.IOUtil;
import th.co.krungthaiaxa.api.elife.utils.EmailSender;
import th.co.krungthaiaxa.api.elife.utils.TestUtil;

import java.util.Collections;

/**
 * @author khoi.tran on 11/23/16.
 *         This class is used for manually integration email testing so that you can check the real email which sent to client.
 *         Please don't use GreenMail mocking.
 *         After testing, we should disable UnitTest, otherwise, the UnitTest will be fail.
 */
//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
//@WebAppConfiguration
//@ActiveProfiles("test")
public class EmailSenderTest {
    //    @Autowired
    EmailSender emailSender;

    //    @Test
    public void sendEmail() {
        String emailContent = IOUtil.loadTextFileInClassPath("/products/iprotect/email-quote.html");
        emailSender.sendEmail("khoi.tran.ags@gmail.com", TestUtil.TESTING_HOTMAIL_JO, "Test email iProtect", emailContent, Collections.emptyList(), Collections.emptyList());
    }
}
