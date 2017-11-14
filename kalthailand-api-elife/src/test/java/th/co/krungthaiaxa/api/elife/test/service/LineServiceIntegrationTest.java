package th.co.krungthaiaxa.api.elife.test.service;

import th.co.krungthaiaxa.api.elife.line.LinePayService;

import java.io.IOException;

//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
//@WebAppConfiguration
//@ActiveProfiles("test")
public class LineServiceIntegrationTest {

    //    @Inject
    private LinePayService linePayService;

    //    @Test
    public void should_send_sms() throws IOException {
        linePayService.sendPushNotificationWithIOException("testnotification", "B6861355");
    }

}
