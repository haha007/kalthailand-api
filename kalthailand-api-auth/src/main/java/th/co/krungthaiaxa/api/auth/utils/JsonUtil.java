package th.co.krungthaiaxa.api.auth.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class JsonUtil {
    private static ObjectMapper mapper = configureObjectMapper();

    public static String getJson(Object source) {
        try {
            return new String(mapper.writeValueAsBytes(source), Charset.forName("UTF-8"));
        } catch (JsonProcessingException e) {
            return new String(e.getMessage().getBytes(StandardCharsets.UTF_8), Charset.forName("UTF-8"));
        }
    }

    private static ObjectMapper configureObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false);
        mapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        mapper.findAndRegisterModules();
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        DefaultSerializerProvider.Impl sp = new DefaultSerializerProvider.Impl();
        mapper.setSerializerProvider(sp);
        return mapper;
    }
}
