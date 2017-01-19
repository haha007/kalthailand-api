package th.co.krungthaiaxa.api.elife.utils;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.springframework.http.ResponseEntity;
import th.co.krungthaiaxa.api.common.utils.ObjectMapperUtil;

import java.util.Collection;
import java.util.List;

/**
 * @author khoi.tran on 12/4/16.
 */
public class HttpResponseAssertUtil {
    public static <C extends Collection<E>, E> C assertResponseCollectionClass(ObjectMapper objectMapper, ResponseEntity<String> responseEntity, Class<C> collectionClass, Class<E> elementClass) {
        CollectionType collectionType = TypeFactory.defaultInstance().constructCollectionType(collectionClass, elementClass);
        return ObjectMapperUtil.toObject(objectMapper, responseEntity.getBody(), collectionType);
    }

    public static <C extends Iterable<E>, E> C assertResponseIterableClass(ObjectMapper objectMapper, ResponseEntity<String> responseEntity, Class<C> parentClass, Class<E> elementClass) {
        JavaType javaType = TypeFactory.defaultInstance().constructParametrizedType(parentClass, null, elementClass);
        return ObjectMapperUtil.toObject(objectMapper, responseEntity.getBody(), javaType);
    }

    /**
     * If your responseContent is Page<E>, please use this method because Page class doesn't have default constructor.
     *
     * @param responseEntity
     * @param elementClass
     * @param <E>
     * @return
     */
    public static <E> List<E> assertResponsePageClass(ObjectMapper objectMapper, ResponseEntity<String> responseEntity, Class<E> elementClass) {
        JsonNode node = ObjectMapperUtil.toObject(objectMapper, responseEntity.getBody());
        JsonNode pageContent = node.findValue("content");
        String jsonContent = pageContent.toString();

        JavaType listJavaType = TypeFactory.defaultInstance().constructCollectionType(List.class, elementClass);
        return ObjectMapperUtil.toObject(objectMapper, jsonContent, listJavaType);
    }

    public static <E> E assertResponseClass(ObjectMapper objectMapper, ResponseEntity<String> responseEntity, Class<E> policyClass) {
        return ObjectMapperUtil.toObject(objectMapper, responseEntity.getBody(), policyClass);
    }
}
