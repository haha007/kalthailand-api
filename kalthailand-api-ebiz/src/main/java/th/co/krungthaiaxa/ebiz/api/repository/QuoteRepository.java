package th.co.krungthaiaxa.ebiz.api.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import th.co.krungthaiaxa.ebiz.api.model.Quote;

public interface QuoteRepository extends MongoRepository<Quote, String> {
}
