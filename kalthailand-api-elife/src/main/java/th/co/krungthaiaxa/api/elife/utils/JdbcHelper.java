package th.co.krungthaiaxa.api.elife.utils;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import th.co.krungthaiaxa.api.common.model.cache.PermanentMemoryCache;

import java.util.Map;

/**
 * @author khoi.tran on 12/7/16.
 */
@Component
public class JdbcHelper {
    private static final Map<Class, RowMapper> rowMappersCache = new PermanentMemoryCache<>();

    /**
     * This method has cache.
     *
     * @param beanClass
     * @param <T>
     * @return
     */
    public <T> RowMapper<T> getRowMapper(Class<T> beanClass) {
        RowMapper<T> result = rowMappersCache.get(beanClass);
        if (result == null) {
            result = BeanPropertyRowMapper.newInstance(beanClass);
            rowMappersCache.put(beanClass, result);
        }
        return result;
    }
}
