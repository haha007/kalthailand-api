package th.co.krungthaiaxa.api.elife.test.resource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import th.co.krungthaiaxa.api.common.model.error.Error;
import th.co.krungthaiaxa.api.common.model.error.ErrorCode;
import th.co.krungthaiaxa.api.common.utils.ErrorUtil;
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
public class PolicyMainInsuredPersonTest extends ELifeTest {
    @Autowired
    private BlackListClient blackListClient;

    @Autowired
    private PolicyFactory policyFactory;

    @Before
    public void mockClients() {
        ELifeTest.mockBlackListClient(blackListClient);
    }

    @Test
    public void receive_bean_validate_error_when_update_person_info_without_required_fields() {
        Policy policy = policyFactory.createPolicyWithPendingPaymentStatus(ProductQuotationFactory.constructIGenDefault());
        PersonInfo personInfo = new PersonInfo();
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(baseUrl + "/policies/" + policy.getPolicyId() + "/main-insured/person", personInfo, String.class);
        Error error = assertError(responseEntity, HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.ERROR_CODE_BEAN_VALIDATION);
        Assert.assertTrue(ErrorUtil.hasFieldError(error, "mobilePhoneNumber"));
        Assert.assertTrue(ErrorUtil.hasFieldError(error, "email"));
    }

}
