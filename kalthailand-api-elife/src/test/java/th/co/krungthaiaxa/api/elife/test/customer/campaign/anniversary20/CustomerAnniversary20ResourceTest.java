package th.co.krungthaiaxa.api.elife.test.customer.campaign.anniversary20;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import th.co.krungthaiaxa.api.common.utils.DateTimeUtil;
import th.co.krungthaiaxa.api.common.utils.IOUtil;
import th.co.krungthaiaxa.api.elife.KalApiElifeApplication;
import th.co.krungthaiaxa.api.elife.customer.campaign.anniversary20.data.CustomerAnniversary20;
import th.co.krungthaiaxa.api.elife.test.ELifeTest;
import th.co.krungthaiaxa.api.elife.utils.TestUtil;

/**
 * @author khoi.tran on 12/26/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
public class CustomerAnniversary20ResourceTest extends ELifeTest {
    @Autowired
    ObjectMapper objectMapper;

    @Test
    public void test_create_customer_anniversary20() {
        String jsonString = IOUtil.loadTextFileInClassPath("/customer/campaign/anniversary20/customer-full.json");

        HttpEntity<String> entity = super.createJsonRequestEntity(jsonString);
        ResponseEntity<String> responseEntity = restTemplate.exchange(super.baseUrl + "/customer/campaign/krungthai-axa-20th-anniversary", HttpMethod.POST, entity, String.class);
        CustomerAnniversary20 result = assertResponseClass(responseEntity, HttpStatus.OK, CustomerAnniversary20.class);
        Assert.assertNotNull(result.getRegistration());
    }

    @Test
    public void test_export_all_customer_anniversary20() {
        ResponseEntity<byte[]> responseEntity = restTemplate.getForEntity(super.baseUrl + "/customer/campaign/krungthai-axa-20th-anniversary/report/download", byte[].class);
        IOUtil.writeBytesToRelativeFile(TestUtil.PATH_TEST_RESULT + "/customer/campaign/krungthai-axa-20th-anniversary/report_" + DateTimeUtil.formatNowForFilePath() + ".xlsx", responseEntity.getBody());
        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }
}
