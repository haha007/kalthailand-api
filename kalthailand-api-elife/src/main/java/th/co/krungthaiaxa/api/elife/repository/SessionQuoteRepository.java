package th.co.krungthaiaxa.api.elife.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.api.elife.model.Quote;
import th.co.krungthaiaxa.api.elife.model.enums.ChannelType;
import th.co.krungthaiaxa.api.elife.data.SessionQuote;

@Repository
public interface SessionQuoteRepository extends PagingAndSortingRepository<SessionQuote, String> {
    SessionQuote findBySessionIdAndChannelType(String sessionId, ChannelType channelType);

    SessionQuote findByQuotesContaining(Quote quote);
}
