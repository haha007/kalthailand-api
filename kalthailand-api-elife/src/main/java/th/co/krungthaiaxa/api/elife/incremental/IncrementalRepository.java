package th.co.krungthaiaxa.api.elife.incremental;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * @author khoi.tran on 10/28/16.
 */
@Repository
public interface IncrementalRepository extends MongoRepository<Incremental, String> {
    Incremental findOneByKey(String incrementalKey);
}
