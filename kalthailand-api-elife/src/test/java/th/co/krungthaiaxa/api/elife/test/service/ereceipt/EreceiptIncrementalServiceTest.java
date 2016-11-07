package th.co.krungthaiaxa.api.elife.test.service.ereceipt;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import th.co.krungthaiaxa.api.elife.KalApiElifeApplication;
import th.co.krungthaiaxa.api.elife.service.ereceipt.EreceiptIncrementalService;
import th.co.krungthaiaxa.api.elife.service.ereceipt.EreceiptNumber;

/**
 * @author khoi.tran on 11/2/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KalApiElifeApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class EreceiptIncrementalServiceTest {

    @Autowired
    private EreceiptIncrementalService ereceiptIncrementalService;

    @Test
    public void test_result_has_enough_properties_with_correct_suffix() {
        EreceiptNumber ereceiptNumber = ereceiptIncrementalService.nextValue(true);
        assertEreceiptNumber(ereceiptNumber);
        Assert.assertEquals(EreceiptIncrementalService.ERECEIPT_NUMBER_SUFFIX_NEWBUSINESS, ereceiptNumber.getSuffixNumberBase36());

        EreceiptNumber ereceiptNumberRenewal = ereceiptIncrementalService.nextValue(false);
        assertEreceiptNumber(ereceiptNumberRenewal);
        Assert.assertEquals(EreceiptIncrementalService.ERECEIPT_NUMBER_SUFFIX_RENEWAL, ereceiptNumberRenewal.getSuffixNumberBase36());
    }

    private void assertEreceiptNumber(EreceiptNumber ereceiptNumber) {
        Assert.assertTrue(StringUtils.isNotBlank(ereceiptNumber.getFullNumberBase36()));
        Assert.assertTrue(ereceiptNumber.getFullNumberBase36().length() <= 8);//It can be less than 8 characters because there's no padding '0'
        Assert.assertTrue(StringUtils.isNotBlank(ereceiptNumber.getMainNumberBase36()));
        Assert.assertTrue(ereceiptNumber.getMainNumberBase36().length() <= 6);
        Assert.assertTrue(ereceiptNumber.getMainNumberDecimal() >= 0);
        Assert.assertTrue(StringUtils.isNotBlank(ereceiptNumber.getSuffixNumberBase36()));
    }

    @Test
    public void test_result_always_increase_regardless_newbusiness_or_not() {
        EreceiptNumber ereceiptNumber01 = ereceiptIncrementalService.nextValue(true);

        EreceiptNumber ereceiptNumber02 = ereceiptIncrementalService.nextValue(false);
        Assert.assertTrue(ereceiptNumber02.getMainNumberDecimal() > ereceiptNumber01.getMainNumberDecimal());

        EreceiptNumber ereceiptNumber03 = ereceiptIncrementalService.nextValue(true);
        Assert.assertTrue(ereceiptNumber03.getMainNumberDecimal() > ereceiptNumber02.getMainNumberDecimal());
    }

    @Test
    public void test_multi_thread_increase_correctly() {

    }
}
