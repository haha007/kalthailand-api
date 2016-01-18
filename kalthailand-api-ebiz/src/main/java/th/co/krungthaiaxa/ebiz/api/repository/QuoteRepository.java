package th.co.krungthaiaxa.ebiz.api.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.ebiz.api.model.Quote;

@Repository
public interface QuoteRepository extends MongoRepository<Quote, String> {
}
