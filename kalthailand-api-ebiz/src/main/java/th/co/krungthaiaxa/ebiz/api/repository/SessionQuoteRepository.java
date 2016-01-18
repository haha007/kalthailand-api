package th.co.krungthaiaxa.ebiz.api.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.ebiz.api.model.enums.SessionType;
import th.co.krungthaiaxa.ebiz.api.model.SessionQuote;

@Repository
public interface SessionQuoteRepository extends MongoRepository<SessionQuote, String> {
    SessionQuote findBySessionIdAndSessionType(String sessionId, SessionType sessionType);
}
