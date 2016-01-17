package th.co.krungthaiaxa.ebiz.api.resource;

import th.co.krungthaiaxa.ebiz.api.model.Quote;
import th.co.krungthaiaxa.ebiz.api.model.error.Error;
import th.co.krungthaiaxa.ebiz.api.utils.JsonUtil;

import java.io.IOException;

public class ResourceTestUtil {

    public static Quote getQuoteFromJSon(String json) throws IOException {
        return JsonUtil.mapper.readValue(json, Quote.class);
    }

    public static Error getErrorFromJSon(String json) throws IOException {
        return JsonUtil.mapper.readValue(json, Error.class);
    }

}
