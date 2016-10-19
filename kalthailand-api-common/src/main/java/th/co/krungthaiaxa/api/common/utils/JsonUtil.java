package th.co.krungthaiaxa.api.common.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.itextpdf.text.log.Logger;
import com.itextpdf.text.log.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class JsonUtil {
    public static final Logger LOGGER = LoggerFactory.getLogger(JsonUtil.class);
    public static ObjectMapper mapper = configureObjectMapper();

    @Deprecated
    public static byte[] getJson(Object source) {
        try {
            return mapper.writeValueAsBytes(source);
        } catch (JsonProcessingException e) {
            LOGGER.error("Cannot write object to json: " + e.getMessage(), e);
            return e.getMessage().getBytes(StandardCharsets.UTF_8);
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
