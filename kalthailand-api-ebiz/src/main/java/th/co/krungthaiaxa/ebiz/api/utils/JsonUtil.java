package th.co.krungthaiaxa.ebiz.api.utils;

import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;

public class JsonUtil {
    public static ObjectMapper mapper = configureObjectMapper();

    private static ObjectMapper configureObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false);
        mapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        DefaultSerializerProvider.Impl sp = new DefaultSerializerProvider.Impl();
        mapper.setSerializerProvider(sp);
        return mapper;
    }

    public static byte[] getJson(Object source) {
        try {
            String json = mapper.writeValueAsString(source);
            return json.getBytes(StandardCharsets.UTF_8);
        } catch (JsonProcessingException e) {
            return e.getMessage().getBytes(StandardCharsets.UTF_8);
        }
    }
}
