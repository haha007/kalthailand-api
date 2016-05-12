package th.co.krungthaiaxa.api.signing;


import th.co.krungthaiaxa.api.signing.utils.JsonUtil;
import th.co.krungthaiaxa.api.signing.model.Error;
import java.io.IOException;

public class TestUtil {

    public static Error getErrorFromJSon(String json) throws IOException {
        return JsonUtil.mapper.readValue(json, Error.class);
    }
}
