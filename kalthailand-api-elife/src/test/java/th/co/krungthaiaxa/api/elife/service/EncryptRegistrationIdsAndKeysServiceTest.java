package th.co.krungthaiaxa.api.elife.service;

import java.lang.reflect.Field;
import java.util.List;

import javax.inject.Inject;

import org.jsoup.helper.StringUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import th.co.krungthaiaxa.api.common.utils.ObjectMapperUtil;
import th.co.krungthaiaxa.api.elife.KalApiApplication;
import th.co.krungthaiaxa.api.elife.model.Coverage;
import th.co.krungthaiaxa.api.elife.model.CoverageBeneficiary;
import th.co.krungthaiaxa.api.elife.model.Insured;
import th.co.krungthaiaxa.api.elife.model.Payment;
import th.co.krungthaiaxa.api.elife.model.Policy;
import th.co.krungthaiaxa.api.elife.model.Registration;
import th.co.krungthaiaxa.api.elife.repository.PaymentRepository;
import th.co.krungthaiaxa.api.elife.repository.PolicyRepository;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class EncryptRegistrationIdsAndKeysServiceTest {

    public static final Logger logger = LoggerFactory.getLogger(EncryptRegistrationIdsAndKeysServiceTest.class);
    private final int PLAIN_TEXT_SIZE = 20;
    @Inject
    private EncryptRegistrationIdsAndKeysService encryptRegistrationIdsAndKeysService;
    @Inject
    private PolicyRepository policyRepository;
    @Inject
    private PaymentRepository paymentRepository;

    private Field fieldRegistrationId;
    private Field fieldRegistrationKey;

    @Before
    public void setUp() throws NoSuchFieldException {
        fieldRegistrationKey = Payment.class.getDeclaredField("registrationKey");
        fieldRegistrationKey.setAccessible(true);

        fieldRegistrationId = Registration.class.getDeclaredField("id");
        fieldRegistrationId.setAccessible(true);
    }

    @After
    public void close() throws NoSuchFieldException {
        fieldRegistrationKey.setAccessible(false);
        fieldRegistrationId.setAccessible(false);
    }

    /**
     * This unit test cannot run concurrency!
     *
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    @Test
    public void should_equals_between_before_encrypt_and_after_encrypt_fo_policy_insured_and_benefit_thai_id_and_line_pay_regkey_success() throws NoSuchFieldException, IllegalAccessException {

        encryptRegistrationIdsAndKeysService.encryptRegistrationIdAndRegistrationKey();

        List<Policy> policies = policyRepository.findAll();

        for (Policy policy : policies) {
            for (Insured insured : policy.getInsureds()) {
                for (Registration registration : insured.getPerson().getRegistrations()) {
                    String registrationId = getRegistrationIdWithoutGetter(registration);
                    Assert.assertTrue("The registration: " + ObjectMapperUtil.toStringMultiLine(registration), isBlankOrEncrypted(registrationId));
                }

            }
            for (Coverage coverage : policy.getCoverages()) {
                for (CoverageBeneficiary coverageBeneficiary : coverage.getBeneficiaries()) {
                    for (Registration registration : coverageBeneficiary.getPerson().getRegistrations()) {
                        String registrationId = getRegistrationIdWithoutGetter(registration);
                        Assert.assertTrue("The registration: " + ObjectMapperUtil.toStringMultiLine(registration), isBlankOrEncrypted(registrationId));
                    }
                }
            }
            for (Payment payment : paymentRepository.findByPolicyId(policy.getPolicyId())) {
                String registrationKey = getRegistrationKeyWithoutGetter(payment);
                Assert.assertTrue("The payment: " + ObjectMapperUtil.toStringMultiLine(payment), isBlankOrEncrypted(registrationKey));
            }
        }
    }

    private String getRegistrationKeyWithoutGetter(Payment payment) throws NoSuchFieldException, IllegalAccessException {
        Object valueObj = fieldRegistrationKey.get(payment);
        return (String) valueObj;
    }

    private String getRegistrationIdWithoutGetter(Registration registration) throws NoSuchFieldException, IllegalAccessException {
        Object valueObj = fieldRegistrationId.get(registration);
        return (String) valueObj;
    }

    private boolean isBlankOrEncrypted(String text) {
        return StringUtil.isBlank(text) || text.length() > PLAIN_TEXT_SIZE;
    }

}
 