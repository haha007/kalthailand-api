package th.co.krungthaiaxa.elife.api.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.elife.api.data.SessionQuote;
import th.co.krungthaiaxa.elife.api.model.Quote;
import th.co.krungthaiaxa.elife.api.model.enums.ChannelType;

@Repository
public interface SessionQuoteRepository extends PagingAndSortingRepository<SessionQuote, String> {
    SessionQuote findBySessionIdAndChannelType(String sessionId, ChannelType channelType);

    SessionQuote findByQuotesContaining(Quote quote);
}
