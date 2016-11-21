package th.co.krungthaiaxa.api.elife.test.resource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import th.co.krungthaiaxa.api.common.basetest.BaseIntegrationResourceTest;
import th.co.krungthaiaxa.api.elife.KalApiElifeApplication;
import th.co.krungthaiaxa.api.elife.client.BlackListClient;
import th.co.krungthaiaxa.api.elife.factory.PolicyFactory;
import th.co.krungthaiaxa.api.elife.factory.productquotation.ProductQuotationFactory;
import th.co.krungthaiaxa.api.elife.model.PersonInfo;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.test.ELifeTest;

/**
 * @author khoi.tran on 11/18/16.
 *         In this test, should not mock ApiFilter.
 */
@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
public class PolicyMainInsuredPersonTest extends BaseIntegrationResourceTest {
    @Autowired
    private BlackListClient blackListClient;

    @Autowired
    private PolicyFactory policyFactory;

    @Before
    public void mockClients() {
        ELifeTest.mockBlackListClient(blackListClient);
    }

    @Test
    public void unauthorized_error_when_update_person_info_without_login() {
        Policy policy = policyFactory.createPolicyWithPendingPaymentStatus(ProductQuotationFactory.constructIGenDefault());
        PersonInfo personInfo = new PersonInfo();
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(baseUrl + "/policies/" + policy.getPolicyId() + "/main-insured/person", personInfo, String.class);
        Assert.assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
    }

//    @Test
//    public void login() {
//        RequestForToken requestForToken = new RequestForToken();
//        requestForToken.setUserName("user1");
//        requestForToken.setPassword("user1");
//        ResponseEntity<String> responseEntity = restTemplate.postForEntity(baseUrl + "/auth/", requestForToken, String.class);
//
//        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
//    }
}
