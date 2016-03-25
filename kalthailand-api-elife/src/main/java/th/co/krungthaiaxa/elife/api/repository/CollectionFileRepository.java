package th.co.krungthaiaxa.elife.api.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.elife.api.data.CollectionFile;

import java.util.List;

@Repository
public interface CollectionFileRepository extends MongoRepository<CollectionFile, String> {
    CollectionFile findByFileHashCode(String fileHashCode);

    List<CollectionFile> findByJobStartedDateNull();
}
