package th.co.krungthaiaxa.api.elife.test;

import org.junit.Assert;
import org.junit.Test;
import th.co.krungthaiaxa.api.elife.filter.KalApiTokenFilter;

/**
 * @author khoi.tran on 11/16/16.
 */
public class KalApiTokenFilterRegExpTest {
    @Test
    public void test() {
        Assert.assertTrue("/api-elife/policies/000000000/main-insured/person?abc=xyz".matches(KalApiTokenFilter.URI_REGEXP_POLICIES_MAIN_INSURED_PERSON));
        Assert.assertTrue("/api-elife/policies/000-000000/main-insured/person?abc=xyz".matches(KalApiTokenFilter.URI_REGEXP_POLICIES_MAIN_INSURED_PERSON));
        Assert.assertTrue("/api-elife/policies/000-000000/main-insured/person".matches(KalApiTokenFilter.URI_REGEXP_POLICIES_MAIN_INSURED_PERSON));
        Assert.assertTrue("/api-elife/policies/000000000/main-insured/person".matches(KalApiTokenFilter.URI_REGEXP_POLICIES_MAIN_INSURED_PERSON));
        Assert.assertTrue("/api-elife/policies/9ab-c99999/main-insured/person".matches(KalApiTokenFilter.URI_REGEXP_POLICIES_MAIN_INSURED_PERSON));
        Assert.assertTrue("/policies/000-000000/main-insured/person".matches(KalApiTokenFilter.URI_REGEXP_POLICIES_MAIN_INSURED_PERSON));
        Assert.assertTrue("/policies/000-000000/main-insured/person?abc=xyz".matches(KalApiTokenFilter.URI_REGEXP_POLICIES_MAIN_INSURED_PERSON));

        Assert.assertFalse("/api-elife/policies/502?2112342/main-insured/person".matches(KalApiTokenFilter.URI_REGEXP_POLICIES_MAIN_INSURED_PERSON));
        Assert.assertFalse("abc??/policies/502?2112342/main-insured/person".matches(KalApiTokenFilter.URI_REGEXP_POLICIES_MAIN_INSURED_PERSON));
        Assert.assertFalse("/policies/502?2112342/main-insured/person".matches(KalApiTokenFilter.URI_REGEXP_POLICIES_MAIN_INSURED_PERSON));

    }
}
