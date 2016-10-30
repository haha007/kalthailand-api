package th.co.krungthaiaxa.api.elife.incremental;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.common.model.cache.PermanentMemoryCache;

import java.util.Map;

/**
 * @author khoi.tran on 10/28/16.
 */
@Service
public class IncrementalService {
    private static Map<String, Long> INCREMENTAL_MAP = new PermanentMemoryCache<>();
    private final IncrementalRepository incrementalRepository;

    @Autowired
    public IncrementalService(IncrementalRepository incrementalRepository) {this.incrementalRepository = incrementalRepository;}

    public synchronized long next(String incrementalKey) {
        Long value = INCREMENTAL_MAP.get(incrementalKey);
        if (value == null) {
            value = loadIncremental(incrementalKey);
            if (value == null) {
                value = 0l;
            }
        } else {
            value++;
        }
        INCREMENTAL_MAP.put(incrementalKey, value);
        saveIncremental(incrementalKey, value);
        return value;
    }

    private void saveIncremental(String incrementalKey, long value) {
        Incremental incremental = incrementalRepository.findOneByKey(incrementalKey);
        incremental.setValue(value);
        incrementalRepository.save(incremental);
    }

    /**
     * @param incrementalKey
     * @return
     */
    private Long loadIncremental(String incrementalKey) {
        Incremental incremental = incrementalRepository.findOneByKey(incrementalKey);
        return (incremental != null) ? incremental.getValue() : null;
    }
}
