package th.co.krungthaiaxa.api.common.model.cache;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author khoi.tran on 10/1/16.
 *         This class will make sure threadsafe.
 *         If you use Map, sometimes you will use HashMap for cache, which is not thread-safe and will cause error when there's many thread access to the same cache.
 */
public class PermanentMemoryCache<K, V> extends ConcurrentHashMap<K, V> {
}
