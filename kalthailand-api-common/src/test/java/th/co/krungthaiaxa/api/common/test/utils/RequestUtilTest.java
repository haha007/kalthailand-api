package th.co.krungthaiaxa.api.common.test.utils;

import org.junit.Assert;
import org.junit.Test;
import th.co.krungthaiaxa.api.common.utils.RequestUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @author khoi.tran on 12/4/16.
 */
public class RequestUtilTest {
    @Test
    public void testSuccessGenerateParameters() {
        Map<String, Object> params = new HashMap<>();
        params.put("policyId", "123");
        params.put("quoteId", null);
        params.put("zzz", 1);
        String paramsString = RequestUtil.generateRequestParameters(params);
        Assert.assertEquals("?policyId=123&zzz=1", paramsString);
    }
}
