package th.co.krungthaiaxa.api.blacklist;


import th.co.krungthaiaxa.api.blacklist.utils.JsonUtil;
import th.co.krungthaiaxa.api.blacklist.model.Error;
import java.io.IOException;

public class TestUtil {

    public static Error getErrorFromJSon(String json) throws IOException {
        return JsonUtil.mapper.readValue(json, Error.class);
    }
}
