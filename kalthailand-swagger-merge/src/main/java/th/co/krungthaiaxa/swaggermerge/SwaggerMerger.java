package th.co.krungthaiaxa.swaggermerge;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import io.swagger.models.*;
import io.swagger.parser.SwaggerParser;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Component
public class SwaggerMerger {
    private static final Logger logger = LoggerFactory.getLogger(SwaggerMerger.class);

    public void start() {
        License license = new License();
        license.setName("license");

        Info info = new Info();
        info.setDescription("API for Krungthai Axa");
        info.setVersion("1.0.0");
        info.setTitle("Krungthai Axa API");
        info.setLicense(license);

        Swagger mergedSwagger = new Swagger();
        mergedSwagger.setSwagger("2.0");
        mergedSwagger.setInfo(info);
        mergedSwagger.setHost("localhost:8080");
        mergedSwagger.setBasePath("/api-auth, /api-elife, /api-signing");
        mergedSwagger.setTags(new ArrayList<>());
        mergedSwagger.setDefinitions(new HashMap<>());

        Swagger authSwagger = new SwaggerParser().read("api-auth-swagger.json");
        addSwagger(mergedSwagger, authSwagger);

        Swagger elifeSwagger = new SwaggerParser().read("api-elife-swagger.json");
        addSwagger(mergedSwagger, elifeSwagger);

        Swagger signingSwagger = new SwaggerParser().read("api-signing-swagger.json");
        addSwagger(mergedSwagger, signingSwagger);

        Swagger blacklistSwagger = new SwaggerParser().read("api-blacklist-swagger.json");
        addSwagger(mergedSwagger, blacklistSwagger);

        mergedSwagger.setPaths(getAllPaths(authSwagger, elifeSwagger, signingSwagger, blacklistSwagger));

        File result = new File("apis-TH-KAL.json");
        try {
            FileUtils.writeByteArrayToFile(result, getJson(mergedSwagger));
        } catch (IOException e) {
            logger.error("Unable to write result file", e);
        }
    }

    private Map<String, Path> getAllPaths(Swagger... swaggers) {
        Map<String, Path> result = new HashMap<>();
        for (Swagger swagger : swaggers) {
            for (String pathKey : swagger.getPaths().keySet()) {
                if (!result.containsKey(pathKey)) {
                    Path path = swagger.getPaths().get(pathKey);
                    result.put(pathKey, path);
                    logger.info("Added path [" + path + "].");
                }
            }
        }
        return result;
    }

    private void addSwagger(Swagger mergedSwagger, Swagger swagger) {
        for (Tag tag : swagger.getTags()) {
            if (!mergedSwagger.getTags().contains(tag)) {
                mergedSwagger.addTag(tag);
                logger.info("Added tag");
            }
        }
//        for (String pathKey : swagger.getPaths().keySet()) {
//            if (!mergedSwagger.getPaths().containsKey(pathKey)) {
//                Path path = swagger.getPaths().get(pathKey);
//                mergedSwagger.getPaths().put(pathKey, path);
//                logger.info("Added path [" + path + "].");
//            }
//        }
        for (String pathKey : swagger.getDefinitions().keySet()) {
            if (!mergedSwagger.getDefinitions().containsKey(pathKey)) {
                mergedSwagger.getDefinitions().put(pathKey, swagger.getDefinitions().get(pathKey));
                logger.info("Added definition");
            }
        }
    }

    private byte[] getJson(Object source) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.findAndRegisterModules();
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        DefaultSerializerProvider.Impl sp = new DefaultSerializerProvider.Impl();
        mapper.setSerializerProvider(sp);

        return mapper.writeValueAsBytes(source);
    }

}
