package th.co.krungthaiaxa.api.elife.test.service;

import th.co.krungthaiaxa.api.elife.line.LineService;

import java.io.IOException;

//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
//@WebAppConfiguration
//@ActiveProfiles("test")
public class LineServiceIntegrationTest {

    //    @Inject
    private LineService lineService;

    //    @Test
    public void should_send_sms() throws IOException {
        lineService.sendPushNotificationWithIOException("testnotification", "B6861355");
    }

}
