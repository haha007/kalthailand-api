package th.co.krungthaiaxa.ebiz.api.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.ebiz.api.model.SessionQuote;
import th.co.krungthaiaxa.ebiz.api.model.enums.SessionType;

@Repository
public interface SessionQuoteRepository extends PagingAndSortingRepository<SessionQuote, String> {
    SessionQuote findBySessionIdAndSessionType(String sessionId, SessionType sessionType);
}
