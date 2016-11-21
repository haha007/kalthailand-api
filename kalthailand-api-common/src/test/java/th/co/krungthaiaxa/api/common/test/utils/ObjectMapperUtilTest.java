package th.co.krungthaiaxa.api.common.test.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import th.co.krungthaiaxa.api.common.model.error.Error;
import th.co.krungthaiaxa.api.common.utils.ErrorUtil;
import th.co.krungthaiaxa.api.common.utils.IOUtil;

/**
 * @author khoi.tran on 11/21/16.
 */
public class ObjectMapperUtilTest {
    @Test
    public void test_json_to_error_object() {
        String json = IOUtil.loadTextFileInClassPath("/json/Error.json");
        Error error = th.co.krungthaiaxa.api.common.utils.ObjectMapperUtil.toObject(new ObjectMapper(), json, Error.class);
        Assert.assertNotNull(error.getCode());
        Assert.assertTrue(ErrorUtil.hasFieldError(error, "mobilePhoneNumber"));
        Assert.assertTrue(ErrorUtil.hasFieldError(error, "email"));
        Assert.assertNotNull(error.getUserMessage());

    }
}
